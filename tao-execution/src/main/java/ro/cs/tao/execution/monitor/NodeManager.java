package ro.cs.tao.execution.monitor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.execution.local.DefaultSessionFactory;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.utils.async.Parallel;


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
    private Timer nodeInspectTimer;
    private Timer nodeRefreshTimer;
    private NodeInspectTask nodeInspectTask;
    private NodeRefreshTask nodeRefreshTask;
    private volatile boolean refreshInProgress;

    static {
        concurrentCalls = Math.max(2, java.lang.Runtime.getRuntime().availableProcessors() / 4);
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        isAvailable = DefaultSessionFactory.class.getName().equals(configurationManager.getValue("tao.drmaa.sessionfactory"));
        instance = isAvailable ? new NodeManager() : null;
        pollInterval = Integer.parseInt(configurationManager.getValue("topology.node.poll.interval", "15"));
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
        persistenceManager = SpringContextBridge.services().getService(PersistenceManager.class);
        refreshInProgress = false;
        //subscribe(Topics.TOPOLOGY);
    }

    /**
     * Initializes this instance with the list of registered topology nodes.
     */
    public void initialize(List<NodeDescription> nodes) {
        if (nodes != null) {
            for (NodeDescription node : nodes) {
                if (node.getActive()) {
                    String host = node.getId();
                    this.nodes.put(host, node);
                    this.runtimeInfo.put(host, new RuntimeInfo());
                }
            }
            new NodeInspectTask().run();
        }
    }

    /**
     * Starts monitoring the registered nodes.
     */
    public void start() {
        stop();
        this.nodeInspectTask = new NodeInspectTask();
        this.nodeRefreshTask = new NodeRefreshTask();
        if (this.nodeInspectTimer == null) {
            this.nodeInspectTimer = new Timer("node-monitor");
        }
        if (this.nodeRefreshTimer == null) {
            this.nodeRefreshTimer = new Timer("node-refresh");
        }
        this.nodeRefreshTimer.schedule(this.nodeRefreshTask, 0, pollInterval * 4 * 1000);
        this.nodeInspectTimer.schedule(this.nodeInspectTask, pollInterval * 1000, pollInterval * 1000);
    }

    /**
     * Stops monitoring the registered nodes.
     */
    public void stop() {
        if (this.nodeInspectTask != null) {
            this.nodeInspectTask.cancel();
        }
        if (this.nodeInspectTimer != null) {
            this.nodeInspectTimer.purge();
        }
        if (this.nodeRefreshTask != null) {
            this.nodeRefreshTask.cancel();
        }
        if (this.nodeRefreshTimer != null) {
            this.nodeRefreshTimer.purge();
        }
    }

    @Override
    protected void onMessageReceived(Message message) {
        /*String host = message.getItem("node");
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
        }*/
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
                            .sorted(Comparator.comparingDouble(e -> computeLoad(nodes.get(e.getKey()),
                                                                                e.getValue().getCpuTotal(),
                                                                                e.getValue().getAvailableMemory())))
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

    protected class NodeInspectTask extends TimerTask {
        private boolean inProgress;
        NodeInspectTask() {
            this.inProgress = false;
        }
        @Override
        public void run() {
            try {
                if (!this.inProgress && !refreshInProgress) {
                    this.inProgress = true;
                    final Set<String> hosts = new HashSet<>(nodes.keySet());
                    Parallel.ForEach(hosts, concurrentCalls, (h) -> {
                        if (nodes.containsKey(h)) {
                            try {
                                while (refreshInProgress) {
                                    try {
                                        Thread.sleep(500);
                                    } catch (Throwable ignored) { }
                                }
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

    protected class NodeRefreshTask extends TimerTask {
        @Override
        public void run() {
            refreshInProgress = true;
            try {
                List<NodeDescription> allNodes = persistenceManager.getNodes();
                Map<String, NodeDescription> allMap = new HashMap<>();
                for (NodeDescription node : allNodes) {
                    String host = node.getId();
                    if (nodes.containsKey(host)) {
                        if (!node.getActive()) {
                            runtimeInfo.put(host,
                                            new RuntimeInfo() {{
                                                setAvailableMemory(-1);
                                                setDiskTotal(node.getDiskSpaceSizeGB());
                                                setTotalMemory(node.getMemorySizeGB());
                                                setCpuTotal(-1);
                                                setDiskUsed(-1);
                                            }});
                        }
                    } else {
                        if (node.getActive()) {
                            nodes.put(host, node);
                            runtimeInfo.put(host,
                                            new RuntimeInfo() {{
                                                setAvailableMemory(-1);
                                                setDiskTotal(node.getDiskSpaceSizeGB());
                                                setTotalMemory(node.getMemorySizeGB());
                                                setCpuTotal(-1);
                                                setDiskUsed(-1);
                                            }});
                        }
                    }
                    allMap.put(host, node);
                }
                nodes.entrySet().removeIf(current -> !allMap.containsKey(current.getKey()));
                runtimeInfo.entrySet().removeIf(current -> !allMap.containsKey(current.getKey()));
            } catch (Exception e) {
                logger.severe("Error during refreshing nodes list from database: " + e.getMessage());
            } finally {
                refreshInProgress = false;
            }
        }
    }

    private double computeLoad(NodeDescription node, double actualCPU, long freeMemory) {
        double result  = 0.0;
        try {
            double noCpus = node.getProcessorCount();
            double totalMem = node.getMemorySizeGB() * MemoryUnit.GIGABYTE.value();
            result = (actualCPU / noCpus) * (1 - (freeMemory / totalMem));
            return result;
        } finally {
            logger.finest(String.format("Processing pressure for %s: %f", node.getId(), result));
        }
    }
}
