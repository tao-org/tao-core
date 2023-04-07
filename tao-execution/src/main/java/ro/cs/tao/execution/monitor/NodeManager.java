package ro.cs.tao.execution.monitor;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.configuration.ConfigurationProvider;
import ro.cs.tao.docker.ExecutionConfiguration;
import ro.cs.tao.execution.local.DefaultSessionFactory;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.quota.QuotaException;
import ro.cs.tao.quota.QuotaManager;
import ro.cs.tao.quota.UserQuotaManager;
import ro.cs.tao.security.UserPrincipal;
import ro.cs.tao.topology.*;
import ro.cs.tao.utils.ExceptionUtils;
import ro.cs.tao.utils.async.Parallel;
import ro.cs.tao.utils.executors.MemoryUnit;
import ro.cs.tao.utils.executors.NamedThreadPoolExecutor;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Singleton than monitors registered topology nodes for resources availability.
 *
 * @author Cosmin Cara
 */
public class NodeManager extends Notifiable implements NodeOperationListener {
    private static final int concurrentCalls;
    private static final boolean isAvailable;
    private static final NodeManager instance;
    private static final int nodeCreationWaitTime;
    private static final long pollInterval;
    private static final NamedThreadPoolExecutor executor;
    private static final int DEFAULT_NODE_WAIT_TIME = 20000;
    private static final int DEFAULT_KEEP_ALIVE_SECONDS = 1800;
    private static final int POLLING_NODE_WITHOUT_TASKS_SECONDS = 20;
    private static final boolean isDevMode;
    private int nodeLimit;
    private NodeProvider nodeProvider;
    private ExecutionTaskProvider taskProvider;
    private DefaultNodeInfo defaultNodeInfo;
    private final Map<String, NodeRuntime> nodes;
    private Timer nodeInspectTimer;
    private Timer nodeRefreshTimer;
    private NodeInspectTask nodeInspectTask;
    private NodeRefreshTask nodeRefreshTask;
    private volatile boolean refreshInProgress;
    private NodeRuntime lastUsedNode;

    static {
        concurrentCalls = Math.max(2, java.lang.Runtime.getRuntime().availableProcessors() / 4);
        final ConfigurationProvider configurationManager = ConfigurationManager.getInstance();
        isAvailable = DefaultSessionFactory.class.getName().equals(configurationManager.getValue("tao.drmaa.sessionfactory"));
        instance = new NodeManager();
        pollInterval = Integer.parseInt(configurationManager.getValue("topology.node.poll.interval", "15"));
        nodeCreationWaitTime = Integer.parseInt(configurationManager.getValue("topology.node.create.wait", "15"));
        executor = new NamedThreadPoolExecutor("node-manager", 1);
        isDevMode = ExecutionConfiguration.developmentModeEnabled();
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

    protected NodeManager() {
        this.nodes = new HashMap<>();
        this.refreshInProgress = false;
        this.nodeLimit = 0;
    }

    public DefaultNodeInfo getDefaultNodeInfo() {
        if (defaultNodeInfo == null) {
            defaultNodeInfo = new DefaultNodeInfo("TAO-", "Processing node", "taouser", "taopassword");
        }
        return defaultNodeInfo;
    }

    public void setDefaultNodeInfo(DefaultNodeInfo defaultNodeInfo) {
        this.defaultNodeInfo = defaultNodeInfo;
    }

    /**
     * Sets the node provider, which is responsible for retrieving the list of managed nodes.
     *
     * @param nodeProvider  The node provider
     */
    public void setNodeProvider(NodeProvider nodeProvider) { this.nodeProvider = nodeProvider; }

    /**
     * Sets the task provider, which is responsible for retrieving tasks executing on a node.
     * @param taskProvider  The task provider
     */
    public void setTaskProvider(ExecutionTaskProvider taskProvider) { this.taskProvider = taskProvider; }

    /**
     * Returns the maximum number of nodes this instance can manage.
     * A value less than or equal to 0 denotes that the limitation is not supported.
     */
    public int getNodeLimit() { return nodeLimit; }

    /**
     * Sets the maximum number of nodes this instance can manage.
     * A value less than or equal to 0 denotes that the limitation is not supported.
     *
     * @param nodeLimit The node limit
     */
    public void setNodeLimit(int nodeLimit) { this.nodeLimit = nodeLimit; }

    public boolean canCreateNewNodes() { return this.nodeLimit >= 0; }

    /**
     * Initializes this instance with the list of registered topology nodes.
     */
    public void initialize() {
        if (this.taskProvider == null) {
            throw new RuntimeException("TaskProvider is not set");
        }
        if (this.nodeProvider != null) {
            addInitialActiveNodes();
            new NodeInspectTask().run();
        } else {
            throw new RuntimeException("NodeProvider is not set");
        }
    }

    protected final void addInitialActiveNodes() {
        List<NodeDescription> nodes = listNodes();
        for (NodeDescription node : nodes) {
            if (node.getActive() != null && node.getActive()) {
                addNode(node, new RuntimeInfo());
            }
        }
    }

    protected List<NodeDescription> listNodes() throws TopologyException {
        return this.nodeProvider.listNodes();
    }

    public int getActiveNodes() throws TopologyException {
        return this.nodeProvider.listNodes().size();
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
        this.nodeRefreshTimer.schedule(this.nodeRefreshTask, 0, pollInterval * 1000);
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
    public void onCompleted(NodeDescription node, ServiceInstallStatus status) {
        if (status.getServiceName().equals("Node")) {
            final String host = node.getId();
            switch (status.getStatus()) {
                case INSTALLED:
                    addNewInstalledNode(node);
                    break;
                case UNINSTALLED:
                    removeNode(host);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Returns the currently monitored nodes.
     */
    public NodeDescription[] getCurrentNodes() {
        NodeDescription[] currentNodes = new NodeDescription[this.nodes.size()];
        int index = 0;
        for (Map.Entry<String, NodeRuntime> entry : this.nodes.entrySet()) {
            currentNodes[index++] = entry.getValue().getNode();
        }
        return currentNodes;
    }

    /**
     * Returns the description of the first node found available.
     * This method blocks until a node is available.
     * If there is just a single node (i.e. the master) registered, it returns immediately the master node description.
     */
    public NodeData getMasterNode() {
        return new NodeData(TopologyManager.getInstance().getMasterNodeInfo(), 0, 0);
    }

    protected NodeDescription getStartingAvailableNode() {
        //String nodeName = isDevMode ? TopologyManager.getInstance().getMasterNodeInfo().getId() : null;
        final String nodeName = TopologyManager.getInstance().getMasterNodeInfo().getId();
        if (isDevMode) {
            NodeRuntime value = this.nodes.get(nodeName);
            if (value != null) {
                return value.getNode();
            }
        } else {
            if (this.nodes.size() > 1) {
                return this.nodes.entrySet().stream().filter(w -> !w.getKey().equals(nodeName)).findFirst().get().getValue().getNode();
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * Returns the description of the first node found available.
     * This method blocks until a node is available, retrying to find it each 5 seconds.
     * The node choice is made as follows:
     *  1) The memory threshold is checked (i.e. find all nodes that have the available memory greater than or equal to the memory threshold).
     *  2) The disk threshold is checked among the candidates from step 1.
     *  3) The processor usage (per core) is checked among the candidates from step 2.
     *
     * @param cpus 		The number of CPUs normally required by the component
     * @param memory    The memory threshold
     * @param disk      The disk threshold
     */
    public NodeData getAvailableNode(String user, int cpus, long memory, long disk) {
        //final Principal principal = SessionStore.currentContext().getPrincipal();
        final Principal principal = new UserPrincipal(user);
        QuotaManager userQuotaManager = UserQuotaManager.getInstance();
        int requestedCPUs = cpus;
        NodeDescription availableNode = null;//getStartingAvailableNode();
        // First check if there are nodes bound to this user
        final List<Map.Entry<String, NodeRuntime>> userNodes = this.nodes.entrySet().stream()
                                                                         .filter(e -> user.equals(e.getValue().getNode().getUserName()))
                                                                         .collect(Collectors.toList());
        // If not, then use the pool of available nodes
        if (userNodes.isEmpty()) {
            userNodes.addAll(this.nodes.entrySet());
        }
        do {
            try {
            	// check if the user has any CPU limitation
            	int availableCpu = userQuotaManager.getAvailableCpus(principal);
                if ((availableCpu == -1 || availableCpu > 0) && userQuotaManager.checkUserProcessingMemory(principal, (int)memory)) {
                    requestedCPUs = (availableCpu == -1) ? cpus : Math.min(cpus, availableCpu);
                    NodeRuntime result;
                    // First try to find a node different from the last one used
                    List<Map.Entry<String, NodeRuntime>> candidates = userNodes.stream()
                                                                               .filter(e -> {
                                                                                        final NodeRuntime nrw = e.getValue();
                                                                                        return !nrw.equals(lastUsedNode) && nrw.getRuntimeInfo().getAvailableMemory() >= memory;
                                                                                    }).collect(Collectors.toList());
                    Map.Entry<String, NodeRuntime> candidate = candidates.stream().min(Comparator.comparingDouble(e -> {
                        NodeDescription node = nodes.get(e.getKey()).getNode();
                        RuntimeInfo runtimeInfo = e.getValue().getRuntimeInfo();
                        return computeLoad(node, runtimeInfo.getCpuTotal(), runtimeInfo.getAvailableMemory());
                    })).orElse(null);
                    if (candidate != null) {
                        result = candidate.getValue();
                    } else {
                        // And if not available, the first suitable one (may also be the last one used)
                        result = userNodes.stream()
                                          // And if not available, the first suitable one (may also be the last one used)
                                          .filter(e -> e.getValue().getRuntimeInfo().getAvailableMemory() >= memory)
                                          .min(Comparator.comparingDouble(e -> {
                                              NodeDescription node = nodes.get(e.getKey()).getNode();
                                              RuntimeInfo runtimeInfo = e.getValue().getRuntimeInfo();
                                              return computeLoad(node, runtimeInfo.getCpuTotal(), runtimeInfo.getAvailableMemory());
                                          }))
                                          .map(Map.Entry::getValue).orElse(null);
                    }
                    if (result != null) {
                        availableNode = result.getNode();
                        lastUsedNode = result;
                        result.getRuntimeInfo().setAvailableMemory(result.getRuntimeInfo().getAvailableMemory() - memory);
                    }
                }
            } catch (QuotaException e) {
    			logger.info(String.format("Error while computing the CPU and memory quota for the user %s. Error message: %s", principal.getName(), e.getMessage()));
            }
            if (availableNode == null) {
                if (canCreateNewNodes() && (this.nodeLimit == 0 || userNodes.size() < this.nodeLimit)) {
                    try {
                        availableNode = createNodeIfMatchingFlavor(cpus, memory);
                    } catch (RuntimeException exception) {
                        throw exception;
                    } catch (Exception exception) {
                        throw new IllegalStateException("Failed to create node.", exception);
                    }
                    if (availableNode == null) {
                        logger.fine(String.format("No processing node matching the request was found. Will retry [%s]",
                                                  this.nodes.values().stream()
                                                            .map(n -> n.getNode().getId() + " cpu:" + n.getRuntimeInfo().getCpuTotal() + ",mem:" + n.getRuntimeInfo().getAvailableMemory())
                                                          .collect(Collectors.joining("; "))));
                        waitBeforeNextRetry();
                    } else {
                        // new node has been created
                        addNewInstalledNode(availableNode);
                    }
                } else {
                    logger.fine("No processing node was found to be available. Will retry.");
                    waitBeforeNextRetry();
                }
            }
        } while (availableNode == null);
        
        return new NodeData(availableNode, Math.min(requestedCPUs, availableNode.getFlavor().getCpu()), memory);
    }

    protected static void waitBeforeNextRetry() {
        try {
            Thread.sleep(NodeManager.DEFAULT_NODE_WAIT_TIME);
        } catch (InterruptedException ignored) {
            // ignore
        }
    }

    protected NodeDescription createNodeIfMatchingFlavor(int cpus, long memory) throws Exception {
        List<NodeFlavor> nodeFlavors = this.nodeProvider.listFlavors();
        NodeFlavor matchingFlavor = nodeFlavors.stream()
                .filter(f -> f.getCpu() >= 2 * cpus && f.getMemory() >= 4 * memory)
                .min(Comparator.comparingInt(NodeFlavor::getCpu))
                .orElse(null);
        if (matchingFlavor != null) {
            final DefaultNodeInfo nodeInfo = getDefaultNodeInfo();
            final Future<NodeDescription> future = executor.submit(() ->
                    this.nodeProvider.create(matchingFlavor,
                            nodeInfo.getNamePrefix() + System.currentTimeMillis(),
                            nodeInfo.getDescription(),
                            nodeInfo.getUser(),
                            nodeInfo.getPassword()));
            NodeDescription node;
            try {
                node = future.get(nodeCreationWaitTime, TimeUnit.SECONDS);
                // The new node should be added into this.nodes in onCompleted() method
            } catch (Exception e) {
                logger.severe(String.format("Node creation failed. Reason: %s", e.getMessage()));
                throw e;
            }
            return node;
        }
        return null;
    }

    public NodeData getNode(String nodeName) {
        NodeRuntime nodeRuntime = this.nodes.get(nodeName);
        if (nodeRuntime == null) {
            return null;
        }
        NodeDescription node = nodeRuntime.getNode();
        return new NodeData(node, node.getFlavor().getCpu(), node.getFlavor().getMemory());
    }

    /**
     * Returns the last available resource utilisation information for the given host.
     *
     * @param hostName  The node host name
     */
    public RuntimeInfo getNodeSnapshot(String hostName) {
        NodeRuntime nodeRuntime = this.nodes.get(hostName);
        return (nodeRuntime == null) ? null : nodeRuntime.getRuntimeInfo();
    }

    public Map<String, RuntimeInfo> getNodesSnapshot() {
        Map<String, RuntimeInfo> runtimeInfoMap = new HashMap<>(this.nodes.size());
        for (Map.Entry<String, NodeRuntime> entry : this.nodes.entrySet()) {
            runtimeInfoMap.put(entry.getKey(), entry.getValue().getRuntimeInfo());
        }
        return runtimeInfoMap;
    }

    @Override
    protected void onMessageReceived(Message message) {
        // do nothing
    }

    private double computeLoad(NodeDescription node, double actualCPU, long availableMemory) {
        double result = 0.0d;
        try {
            final NodeFlavor flavor = node.getFlavor();
            double noCpus = flavor.getCpu();
            double totalMem = flavor.getMemory() * MemoryUnit.MB.value();
            result = (actualCPU / noCpus) * (1 - (availableMemory / totalMem));
        } finally {
            logger.finest(String.format("Processing pressure for %s: %f", node.getId(), result));
        }
        return result;
    }

    private void addNewInstalledNode(NodeDescription node) {
        addNode(node, new RuntimeInfo());
    }

    protected final void removeNode(String host) {
        synchronized (this.nodes) {
            this.nodes.remove(host);
        }
    }

    protected void uninstallNode(NodeDescription node) {
        removeNode(node.getId());
        TopologyManager.getInstance().removeNode(node.getId());
    }

    private void addNode(NodeDescription node, RuntimeInfo runtimeInfo) {
        synchronized (this.nodes) {
            this.nodes.put(node.getId(), new NodeRuntime(node, runtimeInfo));
        }
    }

    protected RuntimeInfo readNodeRuntimeInfo(NodeDescription node) throws Exception {
        OSRuntimeInfo<RuntimeInfo> osRuntimeInfo = OSRuntimeInfo.createInspector(node.getId(), node.getUserName(), node.getUserPass(), RuntimeInfo.class);
        return osRuntimeInfo.getInfo();
    }

    /**
     * This class contains the node on which a task can be executed together with the number of
     * CPU's and the memory that should be used.
     *
     * @author Lucian Barbulescu
     */
    public static class NodeData {
        private final NodeDescription node;
        private final int cpu;
        private final long memory;

        /**
         * Constructor.
         *
         * @param node the node descriptor
         * @param cpu the number of cpus
         * @param memory the memory
         */
        private NodeData(NodeDescription node, int cpu, long memory) {
            this.node = node;
            this.cpu = cpu;
            this.memory = memory;
        }

        public NodeDescription getNode() {
            return node;
        }

        public int getCpu() {
            return cpu;
        }

        public long getMemory() {
            return memory;
        }
    }

    protected class NodeInspectTask extends TimerTask {

        private boolean inProgress;
        private final boolean shouldRetry;

        private NodeInspectTask() {
            this.inProgress = false;
            this.shouldRetry = Integer.parseInt(ConfigurationManager.getInstance().getValue("topology.node.connection.retries", "-1")) >= 0;
        }

        @Override
        public void run() {
            try {
                if (!this.inProgress && !refreshInProgress) {
                    this.inProgress = true;
                    Set<String> hosts;
                    synchronized (nodes) {
                        hosts = new HashSet<>(nodes.keySet());
                    }
                    Parallel.ForEach(hosts, concurrentCalls, (h) -> {
                        try {
                            while (refreshInProgress) {
                                try {
                                    Thread.sleep(500);
                                } catch (Throwable ignored) {
                                    // ignore
                                }
                            }
                            NodeRuntime nodeRuntime;
                            synchronized (nodes) {
                                nodeRuntime = nodes.get(h);
                            }
                            if (nodeRuntime != null) {
                                if (shouldRetry && nodeRuntime.getFailedAttempts() == 3) {
                                    NodeDescription node = nodeRuntime.getNode();
                                    node.setActive(false);
                                    nodeProvider.update(node);
                                    nodes.remove(h);
                                    return;
                                }
                                final List<ExecutionTask> tasks = taskProvider.listByHost(h);
                                boolean isNodeWithoutTasks = (tasks == null || tasks.size() == 0);
                                final LocalDateTime time = nodeRuntime.getNextKeepAlive();
                                if (time != null && !time.isBefore(LocalDateTime.now())) {
                                    Boolean isVolatile = nodeRuntime.getNode().getVolatile();
                                    if (isNodeWithoutTasks && (isVolatile != null && isVolatile)) {
                                        uninstallNode(nodeRuntime.getNode());
                                        return;
                                    } else {
                                        nodeRuntime.setNextKeepAlive(LocalDateTime.now().plusSeconds(DEFAULT_KEEP_ALIVE_SECONDS));
                                    }
                                }

                                boolean canReadNodeInfo = true;
                                if (isNodeWithoutTasks) {
                                    if (nodeRuntime.getLastUpdatedTimeMilliseconds() > 0) {
                                        // the node has been updated at least one time
                                        ConfigurationProvider cfgManager = ConfigurationManager.getInstance();
                                        String value = cfgManager.getValue("topology.node.without.tasks.polling.interval");
                                        int pollingInterval = (value == null) ? POLLING_NODE_WITHOUT_TASKS_SECONDS : Integer.parseInt(value);
                                        long elapsedTimeInSeconds = (System.currentTimeMillis() - nodeRuntime.getLastUpdatedTimeMilliseconds()) / 1000;
                                        if (elapsedTimeInSeconds < pollingInterval) {
                                            canReadNodeInfo = false;
                                        }
                                    }
                                }
                                if (canReadNodeInfo) {
                                    RuntimeInfo runtimeInfo = null;
                                    try {
                                        runtimeInfo = readNodeRuntimeInfo(nodeRuntime.getNode());
                                    } catch (Exception exception) {
                                        logger.log(Level.SEVERE, "Cannot read node information '" + h + "': " + ExceptionUtils.getExceptionLoggingMessage(exception, 1));
                                        nodeRuntime.incrementFailures();
                                    }
                                    if (runtimeInfo == null) {
                                        NodeFlavor flavor = nodeRuntime.getNode().getFlavor();
                                        runtimeInfo = buildDefaultRuntimeInfo(flavor.getDisk(), flavor.getMemory(), flavor.getCpu());
                                    }
                                    nodeRuntime.setRuntimeInfo(runtimeInfo);
                                    nodeRuntime.setLastUpdatedTimeMilliseconds(System.currentTimeMillis());
                                }
                            }
                        } catch (Exception exception) {
                            logger.log(Level.SEVERE, "Cannot update node information '" + h + "': " + ExceptionUtils.getExceptionLoggingMessage(exception, 1));
                        }
                    });
                }
            } catch (Exception exception) {
                logger.log(Level.SEVERE, "Error during monitoring nodes: " + ExceptionUtils.getExceptionLoggingMessage(exception, 1));
            } finally {
                this.inProgress = false;
            }
        }
    }

    protected class NodeRefreshTask extends TimerTask {

        private NodeRefreshTask() {
        }

        @Override
        public void run() {
            refreshInProgress = true;
            try {
                List<NodeDescription> allNodes = listNodes();
                Set<String> allMap = new HashSet<>();
                double cpuTotal = -1.0d;
                for (NodeDescription node : allNodes) {
                    NodeFlavor flavor = node.getFlavor();
                    String host = node.getId();
                    synchronized (nodes) {
                        NodeRuntime nodeRuntime = nodes.get(host);
                        if (nodeRuntime == null) {
                            // the node does not exist in the list
                            if (node.getActive()) {
                                addNode(node, buildDefaultRuntimeInfo(flavor.getDisk(), flavor.getMemory(), cpuTotal));
                            }
                        } else {
                            // the node already exists in the list
                            if (!node.getActive()) {
                                nodeRuntime.setRuntimeInfo(buildDefaultRuntimeInfo(flavor.getDisk(), flavor.getMemory(), cpuTotal));
                            }
                        }
                    }
                    allMap.add(host);
                }
                synchronized (nodes) {
                    nodes.entrySet().removeIf(currentEntry -> !allMap.contains(currentEntry.getKey()));
                }
            } catch (Exception exception) {
                logger.log(Level.SEVERE, "Error during refreshing nodes list from database: " + ExceptionUtils.getExceptionLoggingMessage(exception, 1));
            } finally {
                refreshInProgress = false;
            }
        }
    }

    private static RuntimeInfo buildDefaultRuntimeInfo(long diskTotal, long totalMemory, double cpuTotal) {
        RuntimeInfo runtimeInfo = new RuntimeInfo();
        runtimeInfo.setAvailableMemory(-1);
        runtimeInfo.setDiskTotal(diskTotal);
        runtimeInfo.setTotalMemory(totalMemory);
        runtimeInfo.setCpuTotal(-1);
        runtimeInfo.setDiskUsed(-1);
        return runtimeInfo;
    }

    private static class NodeRuntime {
        private RuntimeInfo runtimeInfo;
        private final NodeDescription node;
        private long lastUpdatedTimeMilliseconds;
        private LocalDateTime nextKeepAlive;
        private int failedAttempts;

        public NodeRuntime(NodeDescription node, RuntimeInfo runtimeInfo) {
            this.node = node;
            this.runtimeInfo = runtimeInfo;
            this.lastUpdatedTimeMilliseconds = 0L;
            this.nextKeepAlive = LocalDateTime.now().plusSeconds(DEFAULT_KEEP_ALIVE_SECONDS);
        }

        public long getLastUpdatedTimeMilliseconds() {
            return lastUpdatedTimeMilliseconds;
        }

        public void setLastUpdatedTimeMilliseconds(long lastUpdatedTimeMilliseconds) {
            this.lastUpdatedTimeMilliseconds = lastUpdatedTimeMilliseconds;
        }

        public RuntimeInfo getRuntimeInfo() {
            return runtimeInfo;
        }

        public void setRuntimeInfo(RuntimeInfo runtimeInfo) {
            this.runtimeInfo = runtimeInfo;
        }

        public NodeDescription getNode() {
            return node;
        }

        public LocalDateTime getNextKeepAlive() {
            return nextKeepAlive;
        }

        public void setNextKeepAlive(LocalDateTime nextKeepAlive) {
            this.nextKeepAlive = nextKeepAlive;
        }

        public int getFailedAttempts() {
            return failedAttempts;
        }

        public void incrementFailures() {
            this.failedAttempts++;
        }
    }
}
