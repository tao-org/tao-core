package ro.cs.tao.execution.monitor;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.configuration.ConfigurationProvider;
import ro.cs.tao.execution.drmaa.DRMAAExecutor;
import ro.cs.tao.execution.local.DefaultSessionFactory;
import ro.cs.tao.execution.model.ExecutionStrategy;
import ro.cs.tao.execution.model.ExecutionStrategyType;
import ro.cs.tao.execution.model.NodeData;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.messaging.Topic;
import ro.cs.tao.persistence.NodeFlavorProvider;
import ro.cs.tao.persistence.ResourceSubscriptionProvider;
import ro.cs.tao.quota.QuotaException;
import ro.cs.tao.quota.QuotaManager;
import ro.cs.tao.quota.UserQuotaManager;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.security.UserPrincipal;
import ro.cs.tao.serialization.JsonMapper;
import ro.cs.tao.subscription.FlavorSubscription;
import ro.cs.tao.subscription.ResourceSubscription;
import ro.cs.tao.subscription.SubscriptionType;
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
    private static int nodeCreationWaitTime;
    private static long pollInterval;
    private static final NamedThreadPoolExecutor executor;
    private static final int DEFAULT_NODE_WAIT_TIME = 20000;

    static {
        concurrentCalls = Math.max(2, java.lang.Runtime.getRuntime().availableProcessors() / 4);
        final ConfigurationProvider configurationManager = ConfigurationManager.getInstance();
        isAvailable = DefaultSessionFactory.class.getName().equals(configurationManager.getValue(DRMAA_SESSION_FACTORY_KEY));
        instance = new NodeManager();
        pollInterval = Integer.parseInt(configurationManager.getValue(NODE_POLLING_INTERVAL_KEY, "15"));
        nodeCreationWaitTime = Integer.parseInt(configurationManager.getValue(NODE_CREATION_WAIT_TIME_KEY, "60"));
        executor = new NamedThreadPoolExecutor("node-manager", 1);
    }

    private final Map<String, NodeRuntime> nodes;
    private final Map<String, LocalDateTime> nodeUpdates;
    //private static final int POLLING_NODE_WITHOUT_TASKS_SECONDS = 20;
    private String appId;
    private int nodeLimit;
    private NodeProvider nodeProvider;
    private ResourceSubscriptionProvider resourceSubscriptionProvider;
    private NodeFlavorProvider flavorProvider;
    private ExecutionTaskProvider taskProvider;
    private DefaultNodeInfo defaultNodeInfo;
    private Timer nodeInspectTimer;
    private Timer nodeRefreshTimer;
    private NodeInspectTask nodeInspectTask;
    private NodeRefreshTask nodeRefreshTask;
    private volatile boolean refreshInProgress;
    private NodeRuntime lastUsedNode;

    protected NodeManager() {
        this.nodes = new HashMap<>();
        this.refreshInProgress = false;
        this.nodeLimit = 0;
        this.nodeUpdates = new HashMap<>();
        Messaging.subscribe(this, Topic.CONFIGURATION.value());
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

    protected static void waitBeforeNextRetry() {
        try {
            Thread.sleep(NodeManager.DEFAULT_NODE_WAIT_TIME);
        } catch (InterruptedException ignored) {
            // ignore
        }
    }

    private static RuntimeInfo buildDefaultRuntimeInfo(long diskTotal, long totalMemory, double cpuTotal) {
        RuntimeInfo runtimeInfo = new RuntimeInfo();
        runtimeInfo.setAvailableMemory(-1);
        runtimeInfo.setDiskTotal(diskTotal);
        runtimeInfo.setTotalMemory(totalMemory);
        runtimeInfo.setCpuTotal(0);
        runtimeInfo.setDiskUsed(-1);
        return runtimeInfo;
    }

    /**
     * Sets the application identifier.
     *
     * @param id The unique identifier for the application
     */
    public void setApplicationId(String id) {
        this.appId = id;
    }

    /**
     * Retrieves the default node information. If the default node information
     * is not already set, it initializes the details using the configuration provider.
     * The information includes node name prefix, description, user, password, and
     * SSH key used for secure access.
     *
     * @return the {@link DefaultNodeInfo} object containing the default configuration
     *         details for a node.
     */
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

    /**
     * Sets the default node information to be used for initializing and managing nodes.
     *
     */
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
     * Sets the resource subscription provider, which is responsible for managing resource subscriptions
     * and providing related information, such as active and ended subscriptions for a user.
     *
     * @param provider The resource subscription provider to be used
     */
    public void setResourceSubscriptionProvider(ResourceSubscriptionProvider provider) { this.resourceSubscriptionProvider = provider; }

    /**
     * Sets the flavor provider, which is responsible for supplying specific node flavor configurations.
     * The flavor provider determines the characteristics of nodes such as CPU and memory.
     *
     * @param provider The NodeFlavorProvider instance to be used for managing node flavors
     */
    public void setFlavorProvider(NodeFlavorProvider provider) { this.flavorProvider = provider; }

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

    /**
     * Determines whether the creation of new nodes is allowed based on the current node limit.
     *
     * @return true if the node limit is greater than or equal to 0, indicating that new nodes can be created; false otherwise
     */
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

    /**
     * Returns the number of nodes that are active.
     */
    public int getActiveNodesCount() throws TopologyException {
        return (int) nodeProvider.listNodes().stream().filter(n -> n.getActive()).count();
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

        final ResourceSubscription subscription = this.resourceSubscriptionProvider.getUserOpenSubscription(userId);
        NodeData nodeData;
        if (subscription != null) {
            // User has a subscription, will try to use only nodes from it
            do {
                nodeData = getSubscriptionNodeCandidate(subscription, cpus, memory);
                if (nodeData == null) {
                    waitBeforeNextRetry();
                }
            } while (nodeData == null);
            return nodeData;
        } else {
            final Principal principal = new UserPrincipal(userId);
            QuotaManager userQuotaManager = UserQuotaManager.getInstance();
            int requestedCPUs = cpus;
            NodeDescription availableNode = null;
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
                    if ((availableCpu == -1 || availableCpu > 0) && userQuotaManager.checkUserProcessingMemory(principal, (int) memory)) {
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
            nodeData = new NodeData(availableNode, Math.min(requestedCPUs, availableNode.getFlavor().getCpu()), memory);
        }
        return nodeData;
    }

    /**
     * Creates a new worker node allocated to a specific user. The method ensures the user is within
     * their permissible node limit and provisions a node if the requirements are met.
     *
     * @param userId The unique identifier of the user requesting the creation of a worker node.
     *               This is used to validate user-specific limits and associate the node accordingly.
     *
     * @return The {@link NodeDescription} object representing the newly created worker node.
     *         Returns null if the user exceeds their node allocation limit, shared nodes are used,
     *         or the necessary configuration is unavailable.
     *
     * @throws TopologyException If the node creation fails due to errors during the provisioning
     *                           process or other internal issues.
     */
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
                    Messaging.send(userId, Topic.INFORMATION.value(), "Trying to provision node " + finalNode.getId());
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

    /**
     * Generates a new node description object for a user based on the provided parameters.
     * This method creates a unique node identifier and sets various properties required
     * for initializing and managing the node.
     *
     * @param existingUserNodesCount The current number of nodes already allocated to the user.
     *                               Used to generate a unique identifier for the new node.
     * @param userId The unique identifier of the user for whom the node is being created.
     * @param matchingFlavor The specific flavor (configuration) of the node to be assigned.
     *                       It defines the characteristics of the node such as CPU and memory.
     * @return A {@link NodeDescription} object that represents the newly created
     *         node, containing updated attributes like ID, flavor, owner, and credentials.
     */
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

    /**
     * Retrieves the description and resource details of a specific node identified by its name.
     *
     * @param nodeName the name of the node to be retrieved
     * @return a {@link NodeData} object containing the node description, CPU, and memory details;
     *         or null if the node with the given name does not exist in the topology
     */
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

    /**
     * Retrieves a snapshot of the runtime information for all registered nodes in the system.
     * This method iterates over the current nodes and collects their associated runtime information.
     *
     * @return a map where the keys are node identifiers (as {@link String}) and the values are the corresponding
     *         {@link RuntimeInfo} objects representing the runtime state of each node.
     */
    public Map<String, RuntimeInfo> getNodesSnapshot() {
        Map<String, RuntimeInfo> runtimeInfoMap = new HashMap<>(this.nodes.size());
        for (Map.Entry<String, NodeRuntime> entry : this.nodes.entrySet()) {
            runtimeInfoMap.put(entry.getKey(), entry.getValue().getRuntimeInfo());
        }
        return runtimeInfoMap;
    }

    /**
     * Initializes and adds the initial set of active nodes to the node management system.
     * This method processes the list of existing nodes, verifies their active status,
     * and excludes nodes with a role equal to NodeRole.K8S. If the node is active and
     * valid, it is added along with its runtime information. Additionally, volatile
     * nodes are tracked by updating their entry in the nodeUpdates map with the
     * current timestamp.
     *
     * This method is primarily responsible for setting up the active node state
     * during system initialization. It ensures that only active, non-K8S nodes
     * are considered and dynamically updates the system for volatile nodes.
     */
    protected final void addInitialActiveNodes() {
        List<NodeDescription> nodes = listNodes();
        for (NodeDescription node : nodes) {
            if (node.getActive() != null && node.getActive() && !NodeRole.K8S.equals(node.getRole())) {
                addNode(node, new RuntimeInfo());
                if (node.getVolatile() != null && node.getVolatile()) {
                    nodeUpdates.put(node.getId(), LocalDateTime.now());
                }
            }
        }
    }

    /**
     * Retrieves a filtered list of active nodes excluding those with the K8S role.
     * The nodes are sourced from the node provider, and only those marked as active
     * and not associated with the K8S role are included in the resulting list.
     *
     * @return a list of NodeDescription objects that are active and do not have the K8S role
     * @throws TopologyException if an error occurs during the node retrieval process
     */
    protected List<NodeDescription> listNodes() throws TopologyException {
        return this.nodeProvider.listNodes().stream().filter(n -> Boolean.TRUE.equals(n.getActive()) &&
                                                                  NodeRole.K8S != n.getRole())
                                .collect(Collectors.toList());
    }

    /**
     * Finds or creates a suitable subscription node candidate based on the provided requirements of CPU and memory,
     * using the subscription's available flavors and the system's current state of nodes.
     * Attempts to reuse an existing node with less than 20% CPU load if available,
     * or provisions a new node if necessary.
     *
     * @param subscription The resource subscription containing the user's details and available flavors.
     * @param cpus The number of CPUs required for the node.
     * @param memory The amount of memory required for the node in bytes.
     * @return A {@code NodeData} object representing the selected or newly created node with allocated CPU and memory,
     *         or {@code null} if no suitable node can be allocated or created.
     */
    protected NodeData getSubscriptionNodeCandidate(ResourceSubscription subscription, int cpus, long memory) {
        NodeDescription node = null;
        // First select the suitable flavors
        final Map<String, FlavorSubscription> flavors = subscription.getFlavors();
        // Flavors will be ordered in ascending order by cpu and then by memory
        final Map<NodeFlavor, Integer> suitableFlavors = new TreeMap<>(new Comparator<NodeFlavor>() {
            @Override
            public int compare(NodeFlavor o1, NodeFlavor o2) {
                final int firstComparison = Integer.compare(o1.getCpu(), o2.getCpu());
                return firstComparison != 0 ? firstComparison : Integer.compare(o1.getMemory(), o2.getMemory());
            }
        });
        for (FlavorSubscription flavor : flavors.values()) {
            final NodeFlavor nodeFlavor = flavorProvider.get(flavor.getFlavorId());
            if (nodeFlavor.getCpu() >= cpus && nodeFlavor.getMemory() >= memory) {
                suitableFlavors.put(nodeFlavor, flavor.getQuantity());
            }
        }
        if (!suitableFlavors.isEmpty()) {
            // Second, try to find an instance of a flavor
            for (Map.Entry<NodeFlavor, Integer> entry : suitableFlavors.entrySet()) {
                final List<Map.Entry<String, NodeRuntime>> list = this.nodes.entrySet().stream()
                                                                            .filter(e -> subscription.getUserId().equals(e.getValue().getNode().getOwner()) &&
                                                                                    e.getValue().getNode().getFlavor().equals(entry.getKey()))
                                                                            .collect(Collectors.toList());
                if (!list.isEmpty()) {
                    final Map<String, Integer> running = taskProvider.countRunning();
                    // Sort the candidates by the number of running tasks, ascending
                    list.sort(new Comparator<Map.Entry<String, NodeRuntime>>() {
                        @Override
                        public int compare(Map.Entry<String, NodeRuntime> o1, Map.Entry<String, NodeRuntime> o2) {
                            return Integer.compare(running.getOrDefault(o1.getKey(), 0),
                                                   running.getOrDefault(o2.getKey(), 0));
                        }
                    });
                    for (Map.Entry<String, NodeRuntime> candidate : list) {
                        final NodeRuntime nodeRuntime = candidate.getValue();
                        final NodeDescription n = nodeRuntime.getNode();
                        final NodeFlavor flavor = n.getFlavor();
                        // Consider an instance with <20% cpu load
                        if (nodeRuntime.getRuntimeInfo().getCpuTotal() <= 20.0) {
                            node = n;
                            break;
                        } else {
                            // Or else decrement the quantity of suitable flavors
                            // or remove the flavor if the quantity reached 0
                            int quantity = suitableFlavors.getOrDefault(flavor, 0) - 1;
                            if (quantity > 0) {
                                suitableFlavors.put(flavor, quantity);
                            } else {
                                suitableFlavors.remove(flavor);
                            }
                        }
                    }
                }
            }
            // If no instance was yet found, check if there are still flavors in the subscription
            if (node == null && !suitableFlavors.isEmpty()) {
                // Pick the first one since the flavors are sorted ascendigly by cpu and then memory
                final NodeFlavor flavor = suitableFlavors.keySet().iterator().next();
                try {
                    final String userId = subscription.getUserId();
                    node = createNode(userId, flavor, (int) this.nodes.entrySet().stream()
                                                                      .filter(e -> userId.equals(e.getValue().getNode().getOwner()))
                                                                      .count(), subscription.getType() == SubscriptionType.PAY_PER_USE);
                    // new node has been created
                    node.setOwner(userId);
                    addNewInstalledNode(node);
                } catch (Exception ignored) {
                    // The exception is logged in the createNode() method
                }
            }
        }
        return node != null
               ? new NodeData(node, Math.min(cpus, node.getFlavor().getCpu()), memory)
               : null;
    }

    /**
     * Creates a new node based on the provided user requirements if a matching flavor is available.
     * Checks the available node flavors to find one that meets or exceeds the specified CPU and memory
     * requirements, and selects one with the minimum CPU that satisfies the criteria. If a match is
     * found, the method creates a new node.
     *
     * @param userNodes a list of user nodes mapped to their respective runtime information
     * @param userId the unique identifier of the user requesting the node
     * @param cpus the minimum number of CPUs required for the node
     * @param memory the minimum amount of memory (in bytes) required for the node
     * @return a {@code NodeDescription} instance representing the created node if a matching flavor
     *         is found, or {@code null} if no matching flavor is available
     * @throws Exception if an error occurs while fetching flavors or creating the node
     */
    protected NodeDescription createNodeIfMatchingFlavor(List<Map.Entry<String, NodeRuntime>> userNodes, String userId, int cpus, long memory) throws Exception {
        List<NodeFlavor> nodeFlavors = this.nodeProvider.listFlavors();
        NodeFlavor matchingFlavor = nodeFlavors.stream()
                                               .filter(f -> f.getCpu() >= cpus && (f.getMemory() * f.getMemoryUnit().value() / MemoryUnit.MB.value()) >= (1.5 * memory))
                                               .min(Comparator.comparingInt(NodeFlavor::getCpu))
                                               .orElse(null);
        if (matchingFlavor != null) {
            return createNode(userId, matchingFlavor, userNodes.size(), true);
        }
        return null;
    }

    /**
     * Creates and provisions a new node based on the given parameters. The method initializes a
     * new node, sends a message regarding its provisioning, and waits for the node to be added
     * to the topology. If the node creation fails, an exception is thrown.
     *
     * @param userId The unique identifier of the user requesting the node creation.
     * @param flavor The node flavor specifying the configuration or type of the node.
     * @param userInstances The number of user instances associated with the node.
     * @param isVolatile A flag indicating whether the node is volatile (temporary) or not.
     * @return The description of the created node.
     * @throws Exception If node creation fails or times out.
     */
    protected NodeDescription createNode(String userId, NodeFlavor flavor, int userInstances, boolean isVolatile) throws Exception {
        NodeDescription node = getNewNodeDescription(userInstances, userId, flavor);
        node.setVolatile(isVolatile);
        final NodeDescription finalNode = node;
        Messaging.send(userId, Topic.INFORMATION.value(), "Trying to provision node " + finalNode.getId());
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

    @Override
    protected void onMessageReceived(Message message) {
        final Map<String, String> items = message.getItems();
        if (items != null) {
            if (items.containsKey(NODE_POLLING_INTERVAL_KEY)) {
                pollInterval = Integer.parseInt(ConfigurationManager.getInstance().getValue(NODE_POLLING_INTERVAL_KEY));
                final long interval = pollInterval * 1000;
                scheduleRefresh(0, interval);
                scheduleInspection(interval, interval);
            } else if (items.containsKey(NODE_CREATION_WAIT_TIME_KEY)) {
                nodeCreationWaitTime = Integer.parseInt(ConfigurationManager.getInstance().getValue(NODE_CREATION_WAIT_TIME_KEY));
            }
        }
    }

    /**
     * Removes a node from the collection of nodes and corresponding updates.
     *
     * @*/
    protected final void removeNode(String host) {
        synchronized (this.nodes) {
            this.nodes.remove(host);
            this.nodeUpdates.remove(host);
        }
    }

    /**
     * Uninstalls the specified node from the system by removing its associated data
     * and updating the topology.
     *
     * @param node the NodeDescription object representing the node to uninstall
     */
    protected void uninstallNode(NodeDescription node) {
        removeNode(node.getId());
        TopologyManager.getInstance().removeNode(node.getId());
    }

    /**
     * Reads and retrieves runtime information of a specified node.
     * The method determines the authentication type based on the presence of an SSH key
     * and uses this information, along with other node attributes, to fetch runtime details.
     * For Linux nodes, the method adjusts the CPU usage percentage as it is reported
     * as the sum of all core loads which may exceed 100%.
     *
     * @param node the node description containing details such as ID, role, authentication, and credentials
     * @return the runtime information of the specified node
     * @throws Exception if an error occurs during the process of fetching runtime information
     */
    protected RuntimeInfo readNodeRuntimeInfo(NodeDescription node) throws Exception {
        AuthenticationType type = node.getSshKey() != null ? AuthenticationType.CERTIFICATE : AuthenticationType.PASSWORD;
        final NodeRuntimeInspector inspector = RuntimeInspectorFactory.getInstance()
                                                                      .get(node.getId(),
                                                                           node.getRole(),
                                                                           type,
                                                                           node.getUserName(),
                                                                           type == AuthenticationType.PASSWORD ? node.getUserPass() : node.getSshKey());
        final RuntimeInfo info = inspector.getInfo();
        if (inspector instanceof LinuxRuntimeInspector) {
            // Linux reports CPU as the sum of the cores load, hence it may be > 100%
            info.setCpuTotal(info.getCpuTotal() / (double) node.getFlavor().getCpu());
        }
        return info;
    }

    /**
     * Schedules a periodic inspection task with the specified delay and interval.
     * Cancels any existing inspection timer before scheduling a new one.
     *
     * @param delay    the delay in milliseconds before the task is executed
     * @param interval the interval in milliseconds between subsequent task executions
     */
    protected void scheduleInspection(long delay, long interval) {
        if (this.nodeInspectTimer != null) {
            this.nodeInspectTimer.cancel();
        }
        this.nodeInspectTimer = new Timer("node-monitor");
        this.nodeInspectTimer.scheduleAtFixedRate(this.nodeInspectTask, delay, interval);
    }

    /**
     * Schedules a periodic refresh task with the specified delay and interval. If a timer
     * is already running, it will be canceled before scheduling a new one.
     *
     * @param delay    the delay in milliseconds before the first execution of the task.
     *                 If the delay is less than or equal to 0, the interval will be used as the delay.
     * @param interval the interval in milliseconds between successive executions of the task
     */
    protected void scheduleRefresh(long delay, long interval) {
        if (this.nodeRefreshTimer != null) {
            this.nodeRefreshTimer.cancel();
        }
        this.nodeRefreshTimer = new Timer("node-refresh");
        this.nodeRefreshTimer.scheduleAtFixedRate(this.nodeRefreshTask, delay > 0 ? delay : interval, interval);
    }

    /**
     * Computes the load of the given node based on its CPU utilization and available memory.
     *
     * @param node the node description containing details such as flavor and identification
     * @param actualCPU the actual CPU usage of the node
     * @param availableMemory the available memory of the node in bytes
     * @return a double value representing the computed load of the node
     */
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
            DRMAAExecutor.setQueueWorkers(getActiveNodesCount());
            nodeUpdates.put(node.getId(), LocalDateTime.now());
        }
    }

    private void addNode(NodeDescription node, RuntimeInfo runtimeInfo) {
        synchronized (this.nodes) {
            this.nodes.put(node.getId(), new NodeRuntime(node, runtimeInfo));
        }
    }

    private boolean useMasterNode() {
        return ConfigurationManager.getInstance().getBooleanValue(USE_MASTER_FOR_EXECUTION_KEY);
    }

    /**
     * Represents the runtime information of a node in a network or system.
     * This class tracks the node's description, runtime information, the last
     * update time, and the number of failed attempts for specific operations.
     */
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

        public NodeDescription getNode() {
            return node;
        }

        public void setNode(NodeDescription node) {
            this.node = node;
        }

        public int getFailedAttempts() {
            return failedAttempts;
        }

        public void incrementFailures() {
            this.failedAttempts++;
        }
    }

    /**
     * The NodeInspectTask class is a scheduled task that performs periodic inspection and management
     * of node runtime information. This task interacts with a collection of nodes, performs operations
     * such as runtime information updates, failure management, and communication with other system components.
     *
     * The class extends {@link TimerTask} and overrides the {@code run()} method to define its specific
     * execution behavior when the task is triggered.
     *
     * Features:
     * - Periodically inspects the runtime state of nodes in the system.
     * - Handles retries for failed node updates and deactivates a node after a threshold of failures.
     * - Updates the runtime information for active nodes and propagates this information using messaging systems.
     * - Ensures synchronization and thread safety when accessing or modifying shared node data.
     */
    protected class NodeInspectTask extends TimerTask {

        private final boolean shouldRetry;
        private boolean inProgress;

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

    /**
     * A task that refreshes the state of nodes in the system. This class extends {@code TimerTask}
     * and is responsible for periodically updating the nodes managed by the application.
     * The task performs operations such as adding new nodes, updating existing nodes,
     * and removing inactive or volatile nodes based on specific conditions.
     *
     * The refresh process:
     * - Retrieves the list of active nodes from the {@code NodeProvider}.
     * - Updates or adds nodes to the internal collection.
     * - Removes nodes that are no longer present or inactive based on the latest data.
     * - Handles the lifecycle of volatile nodes as per the configuration settings.
     *
     * Various operations are synchronized on the internal node collection to ensure thread safety,
     * particularly when modifying the list of nodes.
     *
     * Any errors occurring during the refresh process are logged appropriately.
     */
    protected class NodeRefreshTask extends TimerTask {

        private NodeRefreshTask() {
        }

        @Override
        public void run() {
            refreshInProgress = true;
            try {
                List<NodeDescription> allNodes = nodeProvider.listNodes().stream()
                                                             .filter(n -> Boolean.TRUE.equals(n.getActive()))
                                                             .collect(Collectors.toList());
                Set<String> allMap = new HashSet<>();
                double cpuTotal = -1.0d;
                for (NodeDescription node : allNodes) {
                    NodeFlavor flavor = node.getFlavor();
                    String host = node.getId();
                    synchronized (nodes) {
                        NodeRuntime nodeRuntime = nodes.get(host);
                        if (nodeRuntime == null) {
                            // the node does not exist in the list
                            //if (node.getActive()) {
                            addNode(node, buildDefaultRuntimeInfo(flavor.getDisk(), flavor.getMemory(), cpuTotal));
                            //}
                        } else {
                            // the node already exists in the list
                            if (Boolean.TRUE.equals(nodeRuntime.getNode().getVolatile())) {
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
}
