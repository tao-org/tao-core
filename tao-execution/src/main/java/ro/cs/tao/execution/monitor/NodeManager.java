package ro.cs.tao.execution.monitor;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.configuration.ConfigurationProvider;
import ro.cs.tao.execution.ExecutionConfiguration;
import ro.cs.tao.execution.local.DefaultSessionFactory;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.quota.QuotaException;
import ro.cs.tao.quota.QuotaManager;
import ro.cs.tao.quota.UserQuotaManager;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.topology.*;
import ro.cs.tao.utils.ExceptionUtils;
import ro.cs.tao.utils.async.Parallel;
import ro.cs.tao.utils.executors.NamedThreadPoolExecutor;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

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
    private static final int pollInterval;
    private static final NamedThreadPoolExecutor executor;
    private static final int DEFAULT_NODE_WAIT_TIME = 10000;
    private static final int DEFAULT_KEEP_ALIVE_SECONDS = 1800;
    private static final boolean isDevMode;
    private int nodeLimit;
    private NodeProvider nodeProvider;
    private ExecutionTaskProvider taskProvider;
    private DefaultNodeInfo defaultNodeInfo;
    private final Map<String, NodeRuntimeWrapper> nodes;
    private Timer nodeInspectTimer;
    private Timer nodeRefreshTimer;
    private final Map<String, LocalDateTime> keepAliveMap;
    private NodeInspectTask nodeInspectTask;
    private NodeRefreshTask nodeRefreshTask;
    private volatile boolean refreshInProgress;

    static {
        concurrentCalls = Math.max(2, java.lang.Runtime.getRuntime().availableProcessors() / 4);
        final ConfigurationProvider configurationManager = ConfigurationManager.getInstance();
        isAvailable = DefaultSessionFactory.class.getName().equals(configurationManager.getValue("tao.drmaa.sessionfactory"));
        instance = isAvailable ? new NodeManager() : null;
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
        this.keepAliveMap = Collections.synchronizedMap(new HashMap<>());
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
     * Sets the node provider, which is responsible of retrieving the list of managed nodes.
     *
     * @param nodeProvider  The node provider
     */
    public void setNodeProvider(NodeProvider nodeProvider) { this.nodeProvider = nodeProvider; }

    /**
     * Sets the task provider, which is responsible of retrieving tasks executing on a node.
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

    public boolean canCreateNewNodes() { return this.nodeLimit > 0; }

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
            if (node.getActive()) {
                addNode(node, new RuntimeInfo());
            }
        }
    }

    protected List<NodeDescription> listNodes() throws TopologyException {
        return this.nodeProvider.listNodes();
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
        for (Map.Entry<String, NodeRuntimeWrapper> entry : this.nodes.entrySet()) {
            currentNodes[index++] = entry.getValue().getNode();
        }
        return currentNodes;
    }

    /**
     * Returns the description of the first node found available.
     * This method blocks until a node is available.
     * If there is just a single node (i.e. the master) registered, it returns immediately the master node description.
     */
    public NodeDescription getFirstAvailableNode() {
        if (this.nodes.size() == 1) {
            NodeRuntimeWrapper firstValue = this.nodes.values().iterator().next();
            return firstValue.getNode();
        }
        return getAvailableNode(0, 0, 0).getNode();
    }

    protected NodeDescription getStartingAvailableNode() {
        //String nodeName = isDevMode ? TopologyManager.getInstance().getMasterNodeInfo().getId() : null;
        if (isDevMode) {
            String nodeName = TopologyManager.getInstance().getMasterNodeInfo().getId();
            NodeRuntimeWrapper value = this.nodes.get(nodeName);
            if (value != null) {
                return value.getNode();
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
    public NodeData getAvailableNode(int cpus, long memory, long disk) {
        final Principal principal = SessionStore.currentContext().getPrincipal();
        QuotaManager userQuotaManager = UserQuotaManager.getInstance();
        int requestedCPUs = cpus;
        NodeDescription availableNode = getStartingAvailableNode();
        while (availableNode == null) {
            try {
            	// check if the user has any CPU limitation
            	int availableCpu = userQuotaManager.getAvailableCpus(principal);
                if ((availableCpu == -1 || availableCpu > 0) && userQuotaManager.checkUserProcessingMemory(principal, (int)memory)) {
                    requestedCPUs = (availableCpu == -1) ? cpus : Math.min(cpus, availableCpu);
                    double previousLoadFactor = Double.NaN;
                    for (Map.Entry<String, NodeRuntimeWrapper> entry : this.nodes.entrySet()) {
                        RuntimeInfo runtimeInfo = entry.getValue().getRuntimeInfo();
                        NodeDescription node = entry.getValue().getNode();
                        if (runtimeInfo.getAvailableMemory() >= memory) {
                            double loadFactor = computeLoad(node, runtimeInfo.getCpuTotal(), runtimeInfo.getAvailableMemory());
                            if (Double.isNaN(previousLoadFactor) || previousLoadFactor > loadFactor) {
                                previousLoadFactor = loadFactor;
                                availableNode = node;
                            }
                        }
                    }
                }
            } catch (QuotaException e) {
    			logger.info(String.format("Error while computing the CPU and memory quota for the user %s. Error message: %s", principal.getName(), e.getMessage()));
                availableNode = null;
            }
            if (availableNode == null) {
                if (canCreateNewNodes() && this.nodes.size() < this.nodeLimit) {
                    try {
                        availableNode = createNodeIfMatchingFlavor(cpus, memory);
                    } catch (RuntimeException exception) {
                        throw exception;
                    } catch (Exception exception) {
                        throw new IllegalStateException("Failed to create node.", exception);
                    }
                    if (availableNode == null) {
                        logger.fine("No processing node matching the request was found. Will retry in 10 seconds.");
                        waitBeforeNextRetry(DEFAULT_NODE_WAIT_TIME);
                    } else {
                        // new node has been created
                        addNewInstalledNode(availableNode);
                    }
                } else {
                    logger.fine("No processing node was found to be available. Will retry in 10 seconds.");
                    waitBeforeNextRetry(DEFAULT_NODE_WAIT_TIME);
                }
            }
        }
        
        return new NodeData(availableNode, Math.min(requestedCPUs, availableNode.getFlavor().getCpu()), memory);
    }

    protected static void waitBeforeNextRetry(int timeToWaitMilliseconds) {
        try {
            Thread.sleep(timeToWaitMilliseconds);
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
            try {
                future.get(nodeCreationWaitTime, TimeUnit.SECONDS);
                // The new node should be added into this.nodes in onCompleted() method
            } catch (Exception e) {
                logger.severe(String.format("Node creation failed. Reason: %s", e.getMessage()));
            }
            //TODO Jean return the created node
            return null;
        }
        return null;
    }

    public NodeData getNode(String nodeName) {
        NodeRuntimeWrapper nodeRuntimeWrapper = this.nodes.get(nodeName);
        if (nodeRuntimeWrapper == null) {
            return null;
        }
        NodeDescription node = nodeRuntimeWrapper.getNode();
        return new NodeData(node, node.getFlavor().getCpu(), node.getFlavor().getMemory());
    }

    /**
     * Returns the last available resource utilisation information for the given host.
     *
     * @param hostName  The node host name
     */
    public RuntimeInfo getNodeSnapshot(String hostName) {
        NodeRuntimeWrapper nodeRuntimeWrapper = this.nodes.get(hostName);
        return (nodeRuntimeWrapper == null) ? null : nodeRuntimeWrapper.getRuntimeInfo();
    }

    public Map<String, RuntimeInfo> getNodesSnapshot() {
        Map<String, RuntimeInfo> runtimeInfoMap = new HashMap<>(this.nodes.size());
        for (Map.Entry<String, NodeRuntimeWrapper> entry : this.nodes.entrySet()) {
            runtimeInfoMap.put(entry.getKey(), entry.getValue().getRuntimeInfo());
        }
        return runtimeInfoMap;
    }

    @Override
    protected void onMessageReceived(Message message) {
        // do nothing
    }

    private double computeLoad(NodeDescription node, double actualCPU, long freeMemory) {
        double result = 0.0d;
        try {
            final NodeFlavor flavor = node.getFlavor();
            double noCpus = flavor.getCpu();
            double totalMem = flavor.getMemory() * MemoryUnit.MEGABYTE.value();
            result = (actualCPU / noCpus) * (1 - (freeMemory / totalMem));
        } finally {
            logger.finest(String.format("Processing pressure for %s: %f", node.getId(), result));
        }
        return result;
    }

    private void addNewInstalledNode(NodeDescription node) {
        addNode(node, new RuntimeInfo());
        this.keepAliveMap.put(node.getId(), LocalDateTime.now().plusSeconds(DEFAULT_KEEP_ALIVE_SECONDS));
    }

    protected final void removeNode(String host) {
        synchronized (this.nodes) {
            this.nodes.remove(host);
        }
        this.keepAliveMap.remove(host);
    }

    protected void uninstallNode(NodeDescription node) {
        removeNode(node.getId());
        TopologyManager.getInstance().removeNode(node.getId());
    }

    private void addNode(NodeDescription node, RuntimeInfo runtimeInfo) {
        synchronized (this.nodes) {
            this.nodes.put(node.getId(), new NodeRuntimeWrapper(node, runtimeInfo));
        }
    }

    protected RuntimeInfo readNodeRuntimeInfo(NodeDescription node) throws Exception {
        OSRuntimeInfo osRuntimeInfo = OSRuntimeInfo.createInspector(node.getId(), node.getUserName(), node.getUserPass(), RuntimeInfo.class);
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

        private NodeInspectTask() {
            this.inProgress = false;
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
                            NodeRuntimeWrapper nodeRuntimeWrapper;
                            synchronized (nodes) {
                                nodeRuntimeWrapper = nodes.get(h);
                            }
                            if (nodeRuntimeWrapper != null) {
                                final LocalDateTime time = keepAliveMap.get(h);
                                if (time != null && time.compareTo(LocalDateTime.now()) >= 0) {
                                    final List<ExecutionTask> tasks = taskProvider.listByHost(h);
                                    if (tasks == null || tasks.size() == 0) {
                                        uninstallNode(nodeRuntimeWrapper.getNode());
                                        return;
                                    } else {
                                        keepAliveMap.replace(h, LocalDateTime.now().plusSeconds(DEFAULT_KEEP_ALIVE_SECONDS));
                                    }
                                }

                                RuntimeInfo runtimeInfo = null;
                                try {
                                    runtimeInfo = readNodeRuntimeInfo(nodeRuntimeWrapper.getNode());
                                } catch (Exception ignored) {
                                    // ignore
                                }
                                if (runtimeInfo == null) {
                                    NodeFlavor flavor = nodeRuntimeWrapper.getNode().getFlavor();
                                    runtimeInfo = buildDefaultRuntimeInfo(flavor.getDisk(), flavor.getMemory(), flavor.getCpu());
                                }
                                nodeRuntimeWrapper.setRuntimeInfo(runtimeInfo);
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
                        NodeRuntimeWrapper nodeRuntimeWrapper = nodes.get(host);
                        if (nodeRuntimeWrapper == null) {
                            // the node does not exist in the list
                            if (node.getActive()) {
                                addNode(node, buildDefaultRuntimeInfo(flavor.getDisk(), flavor.getMemory(), cpuTotal));
                            }
                        } else {
                            // the node already exists in the list
                            if (!node.getActive()) {
                                nodeRuntimeWrapper.setRuntimeInfo(buildDefaultRuntimeInfo(flavor.getDisk(), flavor.getMemory(), cpuTotal));
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
        runtimeInfo.setCpuTotal(cpuTotal);
        runtimeInfo.setDiskUsed(-1);
        return runtimeInfo;
    }

    private static class NodeRuntimeWrapper {

        private RuntimeInfo runtimeInfo;
        private NodeDescription node;

        public NodeRuntimeWrapper(NodeDescription node, RuntimeInfo runtimeInfo) {
            this.node = node;
            this.runtimeInfo = runtimeInfo;
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

        public void setNode(NodeDescription node) {
            this.node = node;
        }
    }
}
