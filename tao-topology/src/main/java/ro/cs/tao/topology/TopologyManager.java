package ro.cs.tao.topology;

import ro.cs.tao.messaging.MessageBus;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.spi.ServiceLoader;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.utils.async.BinaryTask;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by cosmin on 8/23/2017.
 */
public class TopologyManager implements ITopologyManager {
    private static final TopologyManager instance;

    private final Logger logger;
    private NodeDescription masterNodeInfo;
    private final Set<TopologyToolInstaller> installers;
    private final PersistenceManager persistenceManager;
    private final ExecutorService executorService;

    static {
        instance = new TopologyManager();
    }

    private TopologyManager() {
        this.logger = Logger.getLogger(TopologyManager.class.getName());
        this.persistenceManager = SpringContextBridge.services().getPersistenceManager();
        this.executorService = Executors.newSingleThreadExecutor();
        // initialize the hostname and ip address in the master node description
        initMasterNodeDescription();

        // load all services for tool installers and initialize the master node info
        ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        ServiceRegistry<TopologyToolInstaller> installersRegistry = serviceRegistryManager.getServiceRegistry(TopologyToolInstaller.class);
        ServiceLoader.loadServices(installersRegistry);
        this.installers = installersRegistry.getServices();

        // Set the autodetermined master node info
        setMasterNodeInfo(masterNodeInfo);
    }

    public static TopologyManager getInstance() {
        return instance;
    }

    @Override
    public NodeDescription get(String hostName) {
        try {
            return persistenceManager.getNodeByHostName(hostName);
        } catch (PersistenceException e) {
            logger.severe("Cannot get node description from database for node " + hostName);
            throw new TopologyException(e);
        }
        // FOR TESTS ONLY
        /*NodeDescription nodeInfo = new NodeDescription();
        nodeInfo.setHostName("node01.testtorque.ro");
        nodeInfo.setUserName("sen2agri");
        nodeInfo.setUserPass("sen2agri");
        nodeInfo.setProcessorCount(2);
        nodeInfo.setMemorySizeGB(16);
        nodeInfo.setDiskSpaceSizeGB(500);
        nodeInfo.setActive(true);
        return nodeInfo;*/
    }

    @Override
    public List<NodeDescription> list() {
        return persistenceManager.getNodes();
    }

    @Override
    public void add(NodeDescription info) throws TopologyException {
        try {
            persistenceManager.saveExecutionNode(info);
        } catch (PersistenceException e) {
            logger.severe("Cannot save node description to database. Rolling back installation on node " + info.getHostName() + "...");
            throw new TopologyException(e);
        }
        // FOR TEST ONLY
        /*Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            private AtomicInteger counter = new AtomicInteger(0);
            @Override
            public void run() {
                TopologyManager.this.executorService.submit(
                        new BinaryTask<NodeDescription, ToolInstallStatus>(info, TopologyManager.this::onCompleted) {
                    @Override
                    public ToolInstallStatus execute(NodeDescription node) {
                        return new ToolInstallStatus() {{
                            setToolName("TestTool");
                            int i = counter.getAndIncrement();
                            if (i % 2 == 0) {
                                setStatus(ServiceStatus.INSTALLED);
                                setReason(String.format("%s completed ok", i));
                            } else {
                                setStatus(ServiceStatus.ERROR);
                                setReason(String.format("Some failure reason %s", i));
                            }
                        }};
                    }
                });
            }
        }, 5000, 2000);*/
        for (TopologyToolInstaller installer: installers) {
            // execute all the installers
            this.executorService.submit(new BinaryTask<NodeDescription, ToolInstallStatus>(info, this::onCompleted) {
                @Override
                public ToolInstallStatus execute(NodeDescription node) {
                    return installer.installNewNode(node);
                }
            });
        }
    }

    @Override
    public void remove(String hostName) {
        NodeDescription node = get(hostName);
        if (node == null) {
            throw new TopologyException(String.format("Node [%s] does not exist", hostName));
        }
        try {
            persistenceManager.deleteExecutionNode(node.getHostName());
        } catch (PersistenceException e) {
            logger.severe("Cannot remove node description from database. Host name is :" + node.getHostName());
            throw new TopologyException(e);
        }
        // execute all the installers
        for (TopologyToolInstaller installer: installers) {
            this.executorService.submit(new BinaryTask<NodeDescription, ToolInstallStatus>(node, this::onCompleted) {
                @Override
                public ToolInstallStatus execute(NodeDescription ref) { return installer.uninstallNode(node); }
            });
        }
    }

    @Override
    public void update(NodeDescription nodeInfo) {
        // execute all the installers
        for (TopologyToolInstaller installer: installers) {
            installer.editNode(nodeInfo);
        }
        try {
            persistenceManager.updateExecutionNode(nodeInfo);
        } catch (PersistenceException e) {
            logger.severe("Cannot update node description in database for the host name:" + nodeInfo.getHostName());
            throw new TopologyException(e);
        }
    }

    public void setMasterNodeInfo(NodeDescription masterNodeInfo) {
        this.masterNodeInfo = masterNodeInfo;
        for (TopologyToolInstaller installer: this.installers) {
            installer.setMasterNodeDescription(masterNodeInfo);
        }
    }

    private void onCompleted(NodeDescription node, ToolInstallStatus status) {
        switch (status.getStatus()) {
            case INSTALLED:
                MessageBus.send(1, MessageBus.INFORMATION,
                                this,
                                String.format("%s installation on %s completed",
                                              status.getToolName(),
                                              node.getHostName()));
                break;
            case UNINSTALLED:
                MessageBus.send(1, MessageBus.INFORMATION,
                                this,
                                String.format("%s uninstallation on %s completed",
                                              status.getToolName(),
                                              node.getHostName()));
                break;
            case ERROR:
                MessageBus.send(1, MessageBus.WARNING,
                                this,
                                String.format("%s installation on %s failed [reason: %s]",
                                              status.getToolName(),
                                              node.getHostName(),
                                              status.getReason()));
                break;
        }
    }

    private void initMasterNodeDescription() throws TopologyException {
        try {
            // TODO: Aparently, the hostname obtained by this method might return a different value
            // than the call to "hostname" call in Linux. Maybe an invocation of hostname will solve the problem
            // but this might break the portability
            masterNodeInfo = new NodeDescription();
            String hostName = InetAddress.getLocalHost().getHostName();
            masterNodeInfo.setHostName(hostName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new TopologyException("Master hostname retrieval failure", e);
        }
    }
}
