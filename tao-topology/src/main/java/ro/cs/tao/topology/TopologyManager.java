package ro.cs.tao.topology;

import ro.cs.tao.bridge.spring.SpringContextBridge;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.spi.ServiceLoader;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by cosmin on 8/23/2017.
 */
public class TopologyManager implements ITopologyManager {
    private static final TopologyManager instance;

    protected Logger logger = Logger.getLogger(TopologyManager.class.getName());
    private ServiceRegistry<ITopologyToolInstaller> installersRegistry;
    private NodeDescription masterNodeInfo;
    private Set<ITopologyToolInstaller> installers;
    private PersistenceManager persistenceManager = SpringContextBridge.services().getPersistenceManager();

    static {
        instance = new TopologyManager();
    }

    private TopologyManager() {
        // initialize the hostname and ip address in the master node description
        initMasterNodeDescription();

        // load all services for tool installers and initialize the master node info
        ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        installersRegistry = serviceRegistryManager.getServiceRegistry(ITopologyToolInstaller.class);
        ServiceLoader.loadServices(installersRegistry);
        this.installers = getInstallerServices();

        // Set the autodetermined master node info
        setMasterNodeInfo(masterNodeInfo);
    }

    public static TopologyManager getInstance() {
        return instance;
    }

    @Override
    public NodeDescription get(String name) {
        try {
            return persistenceManager.getNodeByHostName(name);
        } catch (PersistenceException e) {
            logger.severe("Cannot get node description from database for node " + name);
            throw new TopologyException(e);
        }
    }

    @Override
    public List<NodeDescription> list() {
        return persistenceManager.getNodes();
    }

    @Override
    public void add(NodeDescription info) throws TopologyException {
        // execute all the installers
        for (ITopologyToolInstaller installer: installers) {
            installer.installNewNode(info);
        }
        try {
            persistenceManager.saveExecutionNode(info);
        } catch (PersistenceException e) {
            logger.severe("Cannot save node description to database. Rolling back installation on node " + info.getHostName() + "...");
            for (ITopologyToolInstaller installer: installers) {
                installer.uninstallNode(info);
            }
            throw new TopologyException(e);
        }
    }

    @Override
    public void remove(String name) {
        NodeDescription node = get(name);
        if (node == null) {
            throw new TopologyException(String.format("Node [%s] does not exist", name));
        }
        // execute all the installers
        for (ITopologyToolInstaller installer: installers) {
            installer.uninstallNode(node);
        }
        try {
            // TODO: Update when we have remove execution node
            persistenceManager.saveExecutionNode(node);
        } catch (PersistenceException e) {
            logger.severe("Cannot remove node description from database. Host name is :" + node.getHostName());
            throw new TopologyException(e);
        }
    }

    @Override
    public void update(NodeDescription nodeInfo) {
        // execute all the installers
        for (ITopologyToolInstaller installer: installers) {
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
        for (ITopologyToolInstaller installer: this.installers) {
            installer.setMasterNodeDescription(masterNodeInfo);
        }
    }

    private Set<ITopologyToolInstaller> getInstallerServices() {
        Set<ITopologyToolInstaller> installers = installersRegistry.getServices();
        boolean hasDefaultInstaller = false;
        for(ITopologyToolInstaller installer: installers) {
            if (installer.getClass().isAssignableFrom(DefaultToolInstaller.class)) {
                hasDefaultInstaller = true;
                break;
            }
        }
        if(!hasDefaultInstaller) {
            installers.add(new DefaultToolInstaller());
        }

        return installers;
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