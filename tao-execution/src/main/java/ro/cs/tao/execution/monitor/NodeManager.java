package ro.cs.tao.execution.monitor;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.execution.local.DefaultSessionFactory;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.messaging.Topics;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.utils.Crypto;
import ro.cs.tao.utils.async.Parallel;

import java.util.*;
import java.util.stream.Collectors;

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

    public static boolean isAvailable() { return isAvailable; }

    public static NodeManager getInstance() { return instance; }

    private NodeManager() {
        nodes = new HashMap<>();
        runtimeInfo = new HashMap<>();
        persistenceManager = SpringContextBridge.services().getPersistenceManager();
        subscribe(Topics.TOPOLOGY);
    }

    public void initialize(List<NodeDescription> nodes) {
        if (nodes != null) {
            for (NodeDescription node : nodes) {
                if (node.getActive()) {
                    this.nodes.put(node.getId(), node);
                    this.runtimeInfo.put(node.getId(), new RuntimeInfo());
                }
            }
        }
    }

    public void start() {
        stop();
        this.nodeCheckTask = new NodeCheckTask();
        if (this.nodeCheckTimer == null) {
            this.nodeCheckTimer = new Timer("node-monitor");
        }
        this.nodeCheckTimer.schedule(new NodeCheckTask(), pollInterval, pollInterval);
    }

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

    private synchronized void updateNodeInfo(String host) throws Exception {
        NodeDescription node = nodes.get(host);
        if (node == null) {
            throw new NullPointerException("[no credentials]");
        }
        this.runtimeInfo.put(host, OSRuntimeInfo.createInspector(host, node.getUserName(),
                                                                 Crypto.decrypt(node.getUserPass(), node.getUserName()))
                                                .getSnapshot());
    }

    public NodeDescription[] getCurrentNodes() {
        return nodes.values().toArray(new NodeDescription[0]);
    }

    public NodeDescription getFirstAvailableNode() {
        return nodes.size() == 1 ? nodes.values().iterator().next() : getAvailableNode(0, 0);
    }

    public NodeDescription getAvailableNode(long memory, long disk) {
        String nodeName = null;
        Set<String> candidates = runtimeInfo.entrySet().stream()
                                               .filter(e -> e.getValue().getAvailableMemory() >= memory)
                                               .map(Map.Entry::getKey)
                                               .collect(Collectors.toSet());
        if (candidates.size() > 0) {
            Set<String> remaining = runtimeInfo.entrySet().stream()
                                .filter(e -> (e.getValue().getDiskTotal() - e.getValue().getDiskUsed()) >= disk && candidates.contains(e.getKey()))
                                .map(Map.Entry::getKey).collect(Collectors.toSet());
            if (remaining.size() > 0) {
                nodeName = runtimeInfo.entrySet().stream()
                                .filter(e -> remaining.contains(e.getKey()))
                                .sorted(Comparator.comparingDouble(o -> o.getValue().getCpuTotal() / (double) nodes.get(o.getKey()).getProcessorCount()))
                                .map(Map.Entry::getKey).findFirst().orElse(null);
            }
        }
        return nodeName != null ? nodes.get(nodeName) : null;
    }

    public RuntimeInfo getNodeSnapshot(String hostName) {
        return this.runtimeInfo.get(hostName);
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
