package ro.cs.tao.execution.monitor;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.configuration.ConfigurationProvider;
import ro.cs.tao.execution.local.DefaultSessionFactory;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.quota.QuotaException;
import ro.cs.tao.quota.UserQuotaManager;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.topology.*;
import ro.cs.tao.utils.async.Parallel;
import ro.cs.tao.utils.executors.NamedThreadPoolExecutor;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    private int nodeLimit;
    private NodeProvider nodeProvider;
    private DefaultNodeInfo defaultNodeInfo;
    private final Map<String, NodeDescription> nodes;
    private final Map<String, RuntimeInfo> runtimeInfo;
    private Timer nodeInspectTimer;
    private Timer nodeRefreshTimer;
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
        //persistenceManager = SpringContextBridge.services().getService(PersistenceManager.class);
        refreshInProgress = false;
        nodeLimit = 0;
        //subscribe(Topics.TOPOLOGY);
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
        if (this.nodeProvider != null) {
            final List<NodeDescription> nodes = this.nodeProvider.listNodes();
            for (NodeDescription node : nodes) {
                if (node.getActive()) {
                    String host = node.getId();
                    this.nodes.put(host, node);
                    this.runtimeInfo.put(host, new RuntimeInfo());
                }
            }
            new NodeInspectTask().run();
        } else {
            throw new RuntimeException("NodeProvider is not set");
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
                    this.nodes.put(host, node);
                    this.runtimeInfo.put(host, new RuntimeInfo());
                    break;
                case UNINSTALLED:
                    this.nodes.remove(host);
                    this.runtimeInfo.remove(host);
                    break;
                default:
                    break;
            }
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
            info.setDiskTotal(node.getFlavor().getDisk());
            info.setTotalMemory(node.getFlavor().getMemory());
            info.setCpuTotal(node.getFlavor().getCpu());
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
        return nodes.size() == 1 ? nodes.values().iterator().next() : getAvailableNode(0, 0, 0).getNode();
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
        String nodeName = null;
        final Principal principal = SessionStore.currentContext().getPrincipal();
        int requestedCPUs = cpus;
        while (nodeName == null) {
        	
            try {
            	// check if the user has any CPU limitation
            	final int availableCpu = UserQuotaManager.getInstance().getAvailableCpus(principal);
            	
                if ((availableCpu == -1 || availableCpu > 0) && UserQuotaManager.getInstance().checkUserProcessingMemory(principal, (int)memory)) {
                    requestedCPUs = availableCpu == -1 ? cpus : Math.min(cpus, availableCpu);
                	
                	nodeName = runtimeInfo.entrySet().stream()
                            .filter(e -> e.getValue().getAvailableMemory() >= memory)
                            .filter(e -> (e.getValue().getDiskTotal() - e.getValue().getDiskUsed()) >= disk)
                            .sorted(Comparator.comparingDouble(e -> computeLoad(nodes.get(e.getKey()),
                                                                                e.getValue().getCpuTotal(),
                                                                                e.getValue().getAvailableMemory())))
                            //.sorted(Comparator.comparingDouble(o -> o.getValue().getCpuTotal() / (double) nodes.get(o.getKey()).getProcessorCount()))
                            .map(Map.Entry::getKey).findFirst().orElse(null);
                }
            	
            } catch (QuotaException e) {
    			logger.info(String.format("Error while computing the CPU and memory quota for the user %s. Error message: %s", principal.getName(), e.getMessage()));
    			nodeName = null;
            }
        	
            if (nodeName == null) {
                if (canCreateNewNodes() && this.nodes.size() < this.nodeLimit) {
                    final List<NodeFlavor> nodeFlavors = this.nodeProvider.listFlavors();
                    final NodeFlavor matchingFlavor = nodeFlavors.stream()
                                                                 .filter(f -> f.getCpu() >= 2 * cpus && f.getMemory() >= 4 * memory)
                                                                 .min(Comparator.comparingInt(NodeFlavor::getCpu))
                                                                 .orElse(null);
                    if (matchingFlavor == null) {
                        logger.fine("No processing node matching the request was found. Will retry in 10 seconds.");
                        try {
                            Thread.sleep(DEFAULT_NODE_WAIT_TIME);
                        } catch (InterruptedException ignored) { }
                    } else {
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
                    }
                } else {
                    logger.fine("No processing node was found to be available. Will retry in 10 seconds.");
                    try {
                        Thread.sleep(DEFAULT_NODE_WAIT_TIME);
                    } catch (InterruptedException ignored) { }
                }
            }
        }
        
        final NodeDescription node = nodes.get(nodeName);
        
        return new NodeData(node, Math.min(requestedCPUs, node.getFlavor().getCpu()), memory);
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
                List<NodeDescription> allNodes = nodeProvider.listNodes();
                Map<String, NodeDescription> allMap = new HashMap<>();
                for (NodeDescription node : allNodes) {
                    String host = node.getId();
                    if (nodes.containsKey(host)) {
                        if (!node.getActive()) {
                            runtimeInfo.put(host,
                                            new RuntimeInfo() {{
                                                setAvailableMemory(-1);
                                                setDiskTotal(node.getFlavor().getDisk());
                                                setTotalMemory(node.getFlavor().getMemory());
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
                                                setDiskTotal(node.getFlavor().getDisk());
                                                setTotalMemory(node.getFlavor().getMemory());
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

    private double computeLoad(NodeDescription node, double actualCPU, long freeMemory) {
        double result  = 0.0;
        try {
            final NodeFlavor flavor = node.getFlavor();
            double noCpus = flavor.getCpu();
            double totalMem = flavor.getMemory() * MemoryUnit.GIGABYTE.value();
            result = (actualCPU / noCpus) * (1 - (freeMemory / totalMem));
            return result;
        } finally {
            logger.finest(String.format("Processing pressure for %s: %f", node.getId(), result));
        }
    }
}
