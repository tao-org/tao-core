package ro.cs.tao.execution.monitor;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.configuration.ConfigurationProvider;
import ro.cs.tao.execution.local.DefaultSessionFactory;
import ro.cs.tao.execution.model.ExecutionStrategy;
import ro.cs.tao.execution.model.ExecutionStrategyType;
import ro.cs.tao.execution.model.NodeData;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.messaging.Topic;
import ro.cs.tao.quota.QuotaException;
import ro.cs.tao.quota.QuotaManager;
import ro.cs.tao.quota.UserQuotaManager;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.security.UserPrincipal;
import ro.cs.tao.serialization.JsonMapper;
import ro.cs.tao.topology.*;
import ro.cs.tao.utils.ExceptionUtils;
import ro.cs.tao.utils.async.Parallel;
import ro.cs.tao.utils.executors.AuthenticationType;
import ro.cs.tao.utils.executors.MemoryUnit;
import ro.cs.tao.utils.executors.NamedThreadPoolExecutor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private static final String TOPOLOGY_NODE_NAME_PREFIX_KEY = "topology.node.name.prefix";
    private static final String TOPOLOGY_NODE_DEFAULT_DESCRIPTION_KEY = "topology.node.default.description";
    private static final String TOPOLOGY_MASTER_USER_KEY = "topology.master.user";
    private static final String TOPOLOGY_MASTER_PASSWORD_KEY = "topology.master.password";
    private static final String NODE_USER_LIMIT_KEY = "topology.node.user.limit";
    private static final String NODE_POLLING_INTERVAL_KEY = "topology.node.poll.interval";
    private static final String NODE_CREATION_WAIT_TIME_KEY = "topology.node.create.wait";
    private static final String DRMAA_SESSION_FACTORY_KEY = "tao.drmaa.sessionfactory";
    private static final String OPENSTACK_NODE_USER_KEY = "openstack.node.user";
    private static final String OPENSTACK_NODE_PASSWORD_KEY = "openstack.node.password";
    private static final String USE_MASTER_FOR_EXECUTION_KEY = "topology.use.master.for.execution";
    private static final String NODE_CONNECTION_RETRIES_KEY = "topology.node.connection.retries";
    //private static final String IDLE_NODE_POLLING_INTERVAL_KEY = "topology.node.without.tasks.polling.interval";
    private static final String SSH_KEY = "topology.node.ssh.key";

    private static final int concurrentCalls;
    private static final boolean isAvailable;
    private static final NodeManager instance;
    private static final int nodeCreationWaitTime;
    private static final long pollInterval;
    private static final NamedThreadPoolExecutor executor;
    private static final int DEFAULT_NODE_WAIT_TIME = 20000;
    //private static final int POLLING_NODE_WITHOUT_TASKS_SECONDS = 20;
    private String appId;
    private int nodeLimit;
    private NodeProvider nodeProvider;
    private ExecutionTaskProvider taskProvider;
    private DefaultNodeInfo defaultNodeInfo;
    private final Map<String, NodeRuntime> nodes;
    private final Map<String, LocalDateTime> nodeUpdates;
    private Timer nodeInspectTimer;
    private Timer nodeRefreshTimer;
    private NodeInspectTask nodeInspectTask;
    private NodeRefreshTask nodeRefreshTask;
    private volatile boolean refreshInProgress;
    private NodeRuntime lastUsedNode;

    static {
        concurrentCalls = Math.max(2, java.lang.Runtime.getRuntime().availableProcessors() / 4);
        final ConfigurationProvider configurationManager = ConfigurationManager.getInstance();
        isAvailable = DefaultSessionFactory.class.getName().equals(configurationManager.getValue(DRMAA_SESSION_FACTORY_KEY));
        instance = new NodeManager();
        pollInterval = Integer.parseInt(configurationManager.getValue(NODE_POLLING_INTERVAL_KEY, "15"));
        nodeCreationWaitTime = Integer.parseInt(configurationManager.getValue(NODE_CREATION_WAIT_TIME_KEY, "60"));
        executor = new NamedThreadPoolExecutor("node-manager", 1);
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
        this.nodeUpdates = new HashMap<>();
    }

    public void setApplicationId(String id) {
        this.appId = id;
    }

    public DefaultNodeInfo getDefaultNodeInfo() {
        if (defaultNodeInfo == null) {
            final ConfigurationProvider cfgProvider = ConfigurationManager.getInstance();
            String keyPath = cfgProvider.getValue(SSH_KEY);
            if (keyPath == null) {
                keyPath = cfgProvider.getValue(ConfigurationProvider.APP_HOME) + File.separator + "config" + File.separator + "tao.key";
            }
            try {
                final Path path = Path.of(keyPath);
                if (Files.notExists(path)) {
                    throw new IOException("Private key file not found");
                }
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
            defaultNodeInfo = new DefaultNodeInfo(cfgProvider.getValue(TOPOLOGY_NODE_NAME_PREFIX_KEY,
                                                                       "tao-"),
                                                  cfgProvider.getValue(TOPOLOGY_NODE_DEFAULT_DESCRIPTION_KEY,
                                                                       "Worker node"),
                                                  cfgProvider.getValue(OPENSTACK_NODE_USER_KEY,
                                                                       cfgProvider.getValue(TOPOLOGY_MASTER_USER_KEY)),
                                                  cfgProvider.getValue(OPENSTACK_NODE_PASSWORD_KEY,
                                                                       cfgProvider.getValue(TOPOLOGY_MASTER_PASSWORD_KEY)),
                                                  keyPath);
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
                if (node.getVolatile()) {
                    nodeUpdates.put(node.getId(), LocalDateTime.now());
                }
            }
        }
    }

    protected List<NodeDescription> listNodes() throws TopologyException {
        return this.nodeProvider.listNodes();
    }

    public int getActiveNodes() throws TopologyException {
        return listNodes().size();
    }

    /**
     * Starts monitoring the registered nodes.
     */
    public void start() {
        stop();
        this.nodeInspectTask = new NodeInspectTask();
        this.nodeRefreshTask = new NodeRefreshTask();
        final long interval = pollInterval * 1000;
        scheduleRefresh(0, interval);
        scheduleInspection(interval, interval);
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
        this.nodeUpdates.clear();
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
     * @param strategy  The execution strategy (see {@link ExecutionStrategyType})
     */
    public NodeData getAvailableNode(String userId, int cpus, long memory, ExecutionStrategy strategy) {
        //final Principal principal = SessionStore.currentContext().getPrincipal();
        final Principal principal = new UserPrincipal(userId);
        QuotaManager userQuotaManager = UserQuotaManager.getInstance();
        int requestedCPUs = cpus;
        NodeDescription availableNode = null;//getStartingAvailableNode();
        final boolean shouldPoolNodes = !ConfigurationManager.getInstance().getBooleanValue("topology.dedicated.user.nodes");
        do {
            // First check if there are nodes bound to this user
            final List<Map.Entry<String, NodeRuntime>> userNodes = this.nodes.entrySet().stream()
                                                                             .filter(e -> userId.equals(e.getValue().getNode().getOwner()))
                                                                             .collect(Collectors.toList());
            // If not, then use the pool of available nodes
            if (userNodes.isEmpty() && shouldPoolNodes) {
                userNodes.addAll(this.nodes.entrySet());
                userNodes.removeIf(n -> (!useMasterNode() && n.getValue().getNode().getRole() == NodeRole.MASTER) ||
                                        (strategy.getType() == ExecutionStrategyType.SAME_NODE &&
                                         strategy.getHostName() != null &&
                                         !n.getValue().getNode().getId().equals(strategy.getHostName())));

            }
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
                                                                                        return nodes.size() == 1 || (!nrw.equals(lastUsedNode) && nrw.getRuntimeInfo().getAvailableMemory() >= memory);
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
                    if (result != null && (useMasterNode() || result.getNode().getRole() != NodeRole.MASTER)) {
                        availableNode = result.getNode();
                        lastUsedNode = result;
                        result.getRuntimeInfo().setAvailableMemory(result.getRuntimeInfo().getAvailableMemory() - memory);
                    }
                }
            } catch (QuotaException e) {
    			logger.info(String.format("Error while computing the CPU and memory quota for the user %s. Error message: %s", principal.getName(), e.getMessage()));
            }
            if (availableNode == null) {
                final Map.Entry<String, NodeRuntime> entry = userNodes.stream().filter(e -> e.getValue().getNode().getId().equals(strategy.getHostName())).findFirst().orElse(null);
                if (entry == null) {
                    int nodeUserLimit = Integer.parseInt(ConfigurationManager.getInstance().getValue(NODE_USER_LIMIT_KEY, "1"));
                    if (canCreateNewNodes() && // if node creation is possible
                            ((this.nodeLimit == 0 && userNodes.size() < nodeUserLimit) || // and the user hasn't reached the limit
                                    (userNodes.size() < nodeUserLimit && userNodes.size() + 1 < this.nodeLimit))) { // or creating a new node is below the global limit
                        try {
                            availableNode = createNodeIfMatchingFlavor(userNodes, userId, cpus, memory);
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
                } else {
                    //logger.fine("No processing node was found to be available. Will retry.");
                    //waitBeforeNextRetry();
                    availableNode = entry.getValue().getNode();
                }
            }
        } while (availableNode == null);
        
        return new NodeData(availableNode, Math.min(requestedCPUs, availableNode.getFlavor().getCpu()), memory);
    }

    public NodeDescription createWorkerNode(String userId) throws TopologyException {
        if (TopologyManager.getInstance().isExternalProviderAvailable()) {
            final ConfigurationProvider cfgProvider = ConfigurationManager.getInstance();
            if (cfgProvider.getBooleanValue("topology.dedicated.user.nodes")) {
                // TAO uses dedicated nodes for users
                final int userNodesCount = (int) nodeProvider.listNodes().stream().filter(n -> userId.equals(n.getOwner())).count();
                final int userUsableNodes = nodeProvider.countUsableNodes(userId);
                final int userNodeLimit = Integer.parseInt(cfgProvider.getValue("topology.node.user.limit", "1"));
                // Either the user does not have any node allocated or the node limit for the user has not been reached
                if (userNodesCount == 0 || userUsableNodes < userNodeLimit) {
                    final String flavorId = cfgProvider.getValue("openstack.default.flavour");
                    if (flavorId == null) {
                        logger.warning("Openstack configuration is incomplete, no new node will be created");
                        return null;
                    }
                    final NodeFlavor flavor = this.nodeProvider.listFlavors().stream().filter(f -> f.getId().equals(flavorId)).findFirst().get();
                    NodeDescription node = getNewNodeDescription(0, userId, flavor);
                    final NodeDescription finalNode = node;
                    final Future<NodeDescription> future = executor.submit(() -> TopologyManager.getInstance().addNode(finalNode));
                    try {
                        node = nodeCreationWaitTime > 0
                               ? future.get(nodeCreationWaitTime, TimeUnit.SECONDS)
                               : future.get();
                        // The new node should be added into this.nodes in onCompleted() method
                        // addNewInstalledNode(node);
                    } catch (Exception e) {
                        logger.severe(String.format("Node creation failed. Reason: %s", e.getMessage()));
                        throw new TopologyException(e);
                    }
                    return node;
                }
            } else {
                logger.fine("No new node created, nodes are shared among users");
            }
        } else {
            logger.fine("OpenStack not available, node creation is disabled");
        }
        return null;
    }

    protected static void waitBeforeNextRetry() {
        try {
            Thread.sleep(NodeManager.DEFAULT_NODE_WAIT_TIME);
        } catch (InterruptedException ignored) {
            // ignore
        }
    }

    protected NodeDescription createNodeIfMatchingFlavor(List<Map.Entry<String, NodeRuntime>> userNodes, String userId, int cpus, long memory) throws Exception {
        List<NodeFlavor> nodeFlavors = this.nodeProvider.listFlavors();
        NodeFlavor matchingFlavor = nodeFlavors.stream()
                .filter(f -> f.getCpu() >= cpus && f.getMemory() >= 1.5 * memory)
                .min(Comparator.comparingInt(NodeFlavor::getCpu))
                .orElse(null);
        if (matchingFlavor != null) {
            NodeDescription node = getNewNodeDescription(userNodes.size(), userId, matchingFlavor);
            final NodeDescription finalNode = node;
            final Future<NodeDescription> future = executor.submit(() -> TopologyManager.getInstance().addNode(finalNode));
            try {
                node = nodeCreationWaitTime > 0
                       ? future.get(nodeCreationWaitTime, TimeUnit.SECONDS)
                       : future.get();
                // The new node should be added into this.nodes in onCompleted() method
            } catch (Exception e) {
                logger.severe(String.format("Node creation failed. Reason: %s", e.getMessage()));
                throw e;
            }
            return node;
        }
        return null;
    }

    public NodeDescription getNewNodeDescription(int existingUserNodesCount, String userId, NodeFlavor matchingFlavor) {
        final DefaultNodeInfo nodeInfo = getDefaultNodeInfo();
        NodeDescription node = new NodeDescription();
        node.setFlavor(matchingFlavor);
        node.setId(nodeInfo.getNamePrefix() + userId + "-" + System.currentTimeMillis() + // this is to prevent name duplication in time
                           (existingUserNodesCount < 10 ? "0" : "") + (existingUserNodesCount + 1));
        node.setDescription(nodeInfo.getDescription() + " for " + userId);
        node.setUserName(nodeInfo.getUser());
        node.setUserPass(nodeInfo.getPassword());
        node.setSshKey(nodeInfo.getSshKey());
        node.setVolatile(true);
        node.setRole(NodeRole.WORKER);
        node.setAppId(this.appId);
        node.setOwner(userId);
        return node;
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
        try {
            addNode(node, readNodeRuntimeInfo(node));
        } catch (Exception e) {
            addNode(node, new RuntimeInfo());
        } finally {
            nodeUpdates.put(node.getId(), LocalDateTime.now());
        }
    }

    protected final void removeNode(String host) {
        synchronized (this.nodes) {
            this.nodes.remove(host);
            this.nodeUpdates.remove(host);
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
        AuthenticationType type = node.getSshKey() != null ? AuthenticationType.CERTIFICATE : AuthenticationType.PASSWORD;
        OSRuntimeInfo<RuntimeInfo> osRuntimeInfo = OSRuntimeInfo.createInspector(node.getId(), node.getUserName(),
                                                                                 type == AuthenticationType.PASSWORD
                                                                                    ? node.getUserPass() : node.getSshKey(),
                                                                                 type,
                                                                                 RuntimeInfo.class);
        final RuntimeInfo info = osRuntimeInfo.getInfo();
        if (osRuntimeInfo instanceof OSRuntimeInfo.Linux) {
            // Linux reports CPU as the sum of the cores load, hence it may be > 100%
            info.setCpuTotal(info.getCpuTotal() / (double) node.getFlavor().getCpu());
        }
        return info;
    }

    protected void scheduleInspection(long delay, long interval) {
        if (this.nodeInspectTimer != null) {
            this.nodeInspectTimer.cancel();
        }
        this.nodeInspectTimer = new Timer("node-monitor");
        this.nodeInspectTimer.scheduleAtFixedRate(this.nodeInspectTask, delay, interval);
    }

    protected void scheduleRefresh(long delay, long interval) {
        if (this.nodeRefreshTimer != null) {
            this.nodeRefreshTimer.cancel();
        }
        this.nodeRefreshTimer = new Timer("node-refresh");
        this.nodeRefreshTimer.scheduleAtFixedRate(this.nodeRefreshTask, delay > 0 ? delay : interval, interval);
    }

    private boolean useMasterNode() {
        return ConfigurationManager.getInstance().getBooleanValue(USE_MASTER_FOR_EXECUTION_KEY);
    }

    protected class NodeInspectTask extends TimerTask {

        private boolean inProgress;
        private final boolean shouldRetry;

        private NodeInspectTask() {
            this.inProgress = false;
            this.shouldRetry = Integer.parseInt(ConfigurationManager.getInstance().getValue(NODE_CONNECTION_RETRIES_KEY, "-1")) >= 0;
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
                                    TimeUnit.SECONDS.sleep(500);
                                } catch (InterruptedException ignored) {
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
                                } else {
                                    RuntimeInfo runtimeInfo = null;
                                    try {
                                        runtimeInfo = readNodeRuntimeInfo(nodeRuntime.getNode());
                                    } catch (Exception exception) {
                                        logger.severe(() -> String.format("Cannot read node information '%s': %s",
                                                                          h,
                                                                          ExceptionUtils.getExceptionLoggingMessage(exception, 1)));
                                        nodeRuntime.incrementFailures();
                                    }
                                    if (runtimeInfo == null) {
                                        NodeFlavor flavor = nodeRuntime.getNode().getFlavor();
                                        runtimeInfo = buildDefaultRuntimeInfo(flavor.getDisk(), flavor.getMemory(), flavor.getCpu());
                                    }
                                    nodeRuntime.setRuntimeInfo(runtimeInfo);
                                    nodeRuntime.setLastUpdatedTimeMilliseconds(System.currentTimeMillis());
                                    Messaging.send(SystemPrincipal.instance(),
                                                   Topic.RESOURCES.getCategory(),
                                                   nodeRuntime.node.getId(),
                                                   JsonMapper.instance().writeValueAsString(runtimeInfo),
                                                   false);
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
                            final boolean isVolatile = Boolean.TRUE.equals(nodeRuntime.getNode().getVolatile());
                            if (!node.getActive()) {
                                nodeRuntime.setRuntimeInfo(buildDefaultRuntimeInfo(flavor.getDisk(), flavor.getMemory(), cpuTotal));
                            } else {
                                if (isVolatile) {
                                    // Volatile nodes should be destroyed if no task is running on them for a while
                                    final LocalDateTime now = LocalDateTime.now();
                                    int keepAlive = Integer.parseInt(ConfigurationManager.getInstance()
                                                                                         .getValue("topology.remove.node.after.execution.delay",
                                                                                                   "1800"));
                                    final LocalDateTime lastUpdated = NodeManager.this.nodeUpdates.get(host);
                                    final int recentTasks = taskProvider.countByHostSince(host, now.minusSeconds(keepAlive));
                                    if (recentTasks == 0 && lastUpdated != null && now.minusSeconds(keepAlive).isAfter(lastUpdated)) {
                                        uninstallNode(nodeRuntime.getNode());
                                        nodes.remove(host);
                                    } else {
                                        if (recentTasks > 0) {
                                            NodeManager.this.nodeUpdates.put(host, taskProvider.getLastRunTask(host));
                                        }
                                        // refresh the node information
                                        nodes.get(host).setNode(nodeProvider.getNode(host));
                                    }
                                } else {
                                    // refresh the node information
                                    nodes.get(host).setNode(nodeProvider.getNode(host));
                                }
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

    public static class NodeRuntime {
        private RuntimeInfo runtimeInfo;
        private NodeDescription node;
        private long lastUpdatedTimeMilliseconds;
        private int failedAttempts;

        public NodeRuntime(NodeDescription node, RuntimeInfo runtimeInfo) {
            this.node = node;
            this.runtimeInfo = runtimeInfo;
            this.lastUpdatedTimeMilliseconds = 0L;
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

        public void setNode(NodeDescription node) {
            this.node = node;
        }

        public NodeDescription getNode() {
            return node;
        }

        public int getFailedAttempts() {
            return failedAttempts;
        }

        public void incrementFailures() {
            this.failedAttempts++;
        }
    }
}
