package ro.cs.tao.topology;

import ro.cs.tao.spi.ServiceLoader;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cosmin on 8/23/2017.
 */
public class TopologyManager implements ITopologyManager {
    private ServiceRegistry<ITopologyToolInstaller> installersRegistry;
    private NodeDescription masterNodeInfo;
    private static TopologyManager instance = new TopologyManager();
    Set<ITopologyToolInstaller> installers;

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
    public void addNode(NodeDescription info) {
        // execute all the installers
        for (ITopologyToolInstaller installer: installers) {
            installer.installNewNode(info);
        }
    }

    @Override
    public void removeNode(NodeDescription info) {
        // execute all the installers
        for (ITopologyToolInstaller installer: installers) {
            installer.uninstallNode(info);
        }
    }

    @Override
    public void editNode(NodeDescription nodeInfo) {
        // execute all the installers
        for (ITopologyToolInstaller installer: installers) {
            installer.editNode(nodeInfo);
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
            String hostName = InetAddress.getLocalHost().getHostName();
            masterNodeInfo = new NodeDescription();

            masterNodeInfo.setHostName(hostName);
            //TODO: Get the IP Address - or just not set it and determine it dynamically???
            masterNodeInfo.setIpAddr(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new TopologyException("Master hostname retrieval failure", e);
        }
    }
}
