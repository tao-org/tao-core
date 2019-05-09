package ro.cs.tao.execution.monitor;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.execution.local.DefaultSessionFactory;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.messaging.Topics;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.utils.async.Parallel;

import java.util.*;


/**
 * Singleton than monitors registered topology nodes for resources availability.
 *
 * @author Cosmin Cara
 */
public class NodeManager extends Notifiable {
    private static final int concurrentCalls;
    private static final boolean isAvailable;
    private static final NodeManager instance;
    private static final int pollInterval;
    private final PersistenceManager persistenceManager;
    private final Map<String, NodeDescription> nodes;
    private final Map<String, RuntimeInfo> runtimeInfo;
    private Timer nodeCheckTimer;
    private NodeCheckTask nodeCheckTask;

    static {
        concurrentCalls = Math.max(2, java.lang.Runtime.getRuntime().availableProcessors() / 4);
        isAvailable = DefaultSessionFactory.class.getName().equals(ConfigurationManager.getInstance().getValue("tao.drmaa.sessionfactory"));
        instance = isAvailable ? new NodeManager() : null;
        pollInterval = Integer.parseInt(ConfigurationManager.getInstance().getValue("topology.node.poll.interval", "15"));
    }

    /**
     * Checks if this implementation can be used.
     * It can be used only if the DefaultSessionFactory is configured, otherwise the resource management is supposed
     * to be done by the CRM itself.
     */
    public static boolean isAvailable() { return isAvailable; }

    /**
     * Returns the only instance of this class.
     */
    public static NodeManager getInstance() { return instance; }

    private NodeManager() {
        nodes = new HashMap<>();
        runtimeInfo = new HashMap<>();
        persistenceManager = SpringContextBridge.services().getPersistenceManager();
        subscribe(Topics.TOPOLOGY);
    }

    /**
     * Initializes this instance with the list of registered topology nodes.
     */
    public void initialize(List<NodeDescription> nodes) {
        if (nodes != null) {
            for (NodeDescription node : nodes) {
                if (node.getActive()) {
                    this.nodes.put(node.getId(), node);
                    this.runtimeInfo.put(node.getId(), new RuntimeInfo());
                }
            }
            new NodeCheckTask().run();
        }
    }

    /**
     * Starts monitoring the registered nodes.
     */
    public void start() {
        stop();
        this.nodeCheckTask = new NodeCheckTask();
        if (this.nodeCheckTimer == null) {
            this.nodeCheckTimer = new Timer("node-monitor");
        }
        this.nodeCheckTimer.schedule(new NodeCheckTask(), pollInterval * 1000, pollInterval * 1000);
    }

    /**
     * Stops monitoring the registered nodes.
     */
    public void stop() {
        if (this.nodeCheckTask != null) {
            this.nodeCheckTask.cancel();
        }
        if (this.nodeCheckTimer != null) {
            this.nodeCheckTimer.purge();
        }
    }

    @Override
    protected void onMessageReceived(Message message) {
        String host = message.getItem("node");
        String operation = message.getItem("operation");
        try {
            if ("added".equals(operation)) {
                nodes.put(host, persistenceManager.getNodeByHostName(host));
                updateNodeInfo(host);
            } else if ("removed".equals(operation)) {
                synchronized (this) {
                    runtimeInfo.remove(host);
                    nodes.remove(host);
                }
            } else {
                logger.warning("Unsupported topology operation");
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

    private synchronized void updateNodeInfo(String host) {
        NodeDescription node = nodes.get(host);
        if (node == null) {
            throw new NullPointerException("[no credentials]");
        }
        RuntimeInfo info = null;
        try {
            info = OSRuntimeInfo.createInspector(host, node.getUserName(), node.getUserPass())
                                                             //Crypto.decrypt(node.getUserPass(), node.getUserName()))
                                .getInfo();
        } catch (Exception ignored) { }
        if (info == null) {
            info = new RuntimeInfo();
            info.setAvailableMemory(-1);
            info.setDiskTotal(node.getDiskSpaceSizeGB());
            info.setTotalMemory(node.getMemorySizeGB());
            info.setCpuTotal(-1);
            info.setDiskUsed(-1);
        }
        this.runtimeInfo.put(host, info);
    }

    /**
     * Returns the currently monitored nodes.
     */
    public NodeDescription[] getCurrentNodes() {
        return nodes.values().toArray(new NodeDescription[0]);
    }

    /**
     * Returns the description of the first node found available.
     * This method blocks until a node is available.
     * If there is just a single node (i.e. the master) registered, it returns immediately the master node description.
     */
    public NodeDescription getFirstAvailableNode() {
        return nodes.size() == 1 ? nodes.values().iterator().next() : getAvailableNode(0, 0);
    }

    /**
     * Returns the description of the first node found available.
     * This method blocks until a node is available, retrying to find it each 5 seconds.
     * The node choice is made as follows:
     *  1) The memory threshold is checked (i.e. find all nodes that have the available memory greater than or equal to the memory threshold).
     *  2) The disk threshold is checked among the candidates from step 1.
     *  3) The processor usage (per core) is checked among the candidates from step 2.
     *
     * @param memory    The memory threshold
     * @param disk      The disk threshold
     */
    public NodeDescription getAvailableNode(long memory, long disk) {
        String nodeName = null;
        while (nodeName == null) {
            nodeName = runtimeInfo.entrySet().stream()
                            .filter(e -> e.getValue().getAvailableMemory() >= memory)
                            .filter(e -> (e.getValue().getDiskTotal() - e.getValue().getDiskUsed()) >= disk)
                            .sorted((e1, e2) -> Long.compare(e2.getValue().getAvailableMemory(), e1.getValue().getAvailableMemory()))
                            //.sorted(Comparator.comparingDouble(o -> o.getValue().getCpuTotal() / (double) nodes.get(o.getKey()).getProcessorCount()))
                            .map(Map.Entry::getKey).findFirst().orElse(null);
            if (nodeName == null) {
                logger.fine("No processing node was found to be available. Will retry in 5 seconds.");
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) { }
        }
        return nodes.get(nodeName);
    }

    /**
     * Returns the last available resource utilisation information for the given host.
     *
     * @param hostName  The node host name
     */
    public RuntimeInfo getNodeSnapshot(String hostName) {
        return this.runtimeInfo.get(hostName);
    }

    public Map<String, RuntimeInfo> getNodesSnapshot() {
        return this.runtimeInfo;
    }

    protected class NodeCheckTask extends TimerTask {
        private boolean inProgress;
        NodeCheckTask() {
            this.inProgress = false;
        }
        @Override
        public void run() {
            try {
                if (!this.inProgress) {
                    this.inProgress = true;
                    final Set<String> hosts = new HashSet<>(nodes.keySet());
                    Parallel.ForEach(hosts, concurrentCalls, (h) -> {
                        if (nodes.containsKey(h)) {
                            try {
                                updateNodeInfo(h);
                            } catch (Exception e) {
                                logger.severe(String.format("Cannot update node information [node=%s, error=%s]",
                                                            h, e.getMessage()));
                            }
                        }
                    });
                }
            } catch (Exception e) {
                logger.severe("Error during monitoring nodes: " + e.getMessage());
            } finally {
                this.inProgress = false;
            }

        }
    }
}
