/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.topology;

import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.component.SystemVariable;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.configuration.ConfigurationProvider;
import ro.cs.tao.docker.ExecutionConfiguration;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Topic;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.VolatileInstanceProvider;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.topology.docker.DockerImageInstaller;
import ro.cs.tao.topology.docker.DockerManager;
import ro.cs.tao.topology.docker.SingletonContainer;
import ro.cs.tao.topology.provider.DefaultNodeOperationListener;
import ro.cs.tao.utils.async.BinaryTask;
import ro.cs.tao.utils.executors.*;

import java.net.InetAddress;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Cosmin Udroiu
 */
public class TopologyManager {

    //private static final String createMountTemplate;
    private static final String checkLocalShareWindowsTemplate;
    private static final String createLocalShareWindowsTemplate;
    private static final String createLocalShareLinuxTemplate;
    private static final String createWorkingDirectoryTemplate;
    private static final OutputAccumulator sharedAccumulator;
    private static final TopologyManager instance;
    private static NodeDescription masterNodeInfo;

    private final Logger logger;
    private NodeProvider externalNodeProvider;
    private VolatileInstanceProvider volatileInstanceProvider;
    private NodeProvider localNodeProvider;
    private final Set<NodeOperationListener> listeners;
    private final Set<TopologyToolInstaller> installers;
    private final ExecutorService executorService;

    static {
        //if [ ! -e /mnt/tao ]; then sed -i '$a//#MASTERHOST##SHARE# /mnt/tao cifs user=tao,password=tao123,file_mode=0777,dir_mode=0777,noperm 0 0' /etc/fstab; fi
        //createMountTemplate = "if [ ! -e %1$s ]; then mkdir %1$s; chmod 777 %1$s; sed -i \"$ a//%2$s%3$s %1$s cifs user=%4$s,password=%5$s,file_mode=0777,dir_mode=0777,noperm 0 0\" /etc/fstab; mount -a; fi;";
        checkLocalShareWindowsTemplate = "net share %s";
        createLocalShareWindowsTemplate = "net share %s=%s /GRANT:Everyone,FULL";
        createLocalShareLinuxTemplate = "setenforce 0 && if [ -e $(smbclient -N -g -L localhost | grep \"%1$s\") ]; then sed -i \"$ a[%1$s]\n\tpath = %2$s\n\tbrowsable = yes\n\twritable = yes\n\tguest ok = yes\n\tcreate mask = 0666\n\tforce create mode = 0666\n\tdirectory mask = 0777\n\tforce directory mode = 0777\" /etc/samba/smb.conf; fi;";
        createWorkingDirectoryTemplate = "mount %s " + ExecutionConfiguration.getWorkerContainerVolumeMap().getHostWorkspaceFolder();
        sharedAccumulator = new OutputAccumulator();
        instance = new TopologyManager();
        Executor.setEnvironment(ConfigurationManager.getInstance().getSystemEnvironment());

    }

    private TopologyManager() {
        this.logger = Logger.getLogger(TopologyManager.class.getName());
        this.executorService = new NamedThreadPoolExecutor("topology-thread", 1);//Executors.newSingleThreadExecutor();

        // load all services for tool installers and initialize the master node info
        ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        final ServiceRegistry<TopologyToolInstaller> installersRegistry = serviceRegistryManager.getServiceRegistry(TopologyToolInstaller.class);
        this.installers = installersRegistry.getServices();
        final ServiceRegistry<NodeProvider> nodeProviderRegistry = serviceRegistryManager.getServiceRegistry(NodeProvider.class);
        final Set<NodeProvider> nodeProviders = nodeProviderRegistry.getServices();
        final String configuredProvider = ConfigurationManager.getInstance().getValue("topology.provider");
        logger.info(String.format("External node providers available: %s",
                                  nodeProviders.isEmpty() ? "none" :
                                  nodeProviders.stream().map(p -> p.getClass().getName()).collect(Collectors.joining(","))));
        for (NodeProvider provider : nodeProviders) {
            if (provider.getClass().getName().equals(configuredProvider)) {
                this.externalNodeProvider = provider;
            }
        }

        if (this.externalNodeProvider == null) {
            logger.info("No external node provider configured");
        } else {
            logger.info(String.format("External node provider '%s' registered", this.externalNodeProvider.getClass().getSimpleName()));
            try {
                this.externalNodeProvider.authenticate();
            } catch (Throwable e) {
                logger.severe(e.getMessage());
            }
        }
        this.listeners = new HashSet<>();
        registerListener(new DefaultNodeOperationListener());
    }

    /**
     * Provides the singleton instance of the TopologyManager.
     *
     * @return the singleton instance of TopologyManager
     */
    public static TopologyManager getInstance() {
        return instance;
    }

    /**
     * Sets the master node information for the topology.
     *
     * @param master the NodeDescription object representing the master node
     */
    public static void setMasterNode(NodeDescription master) {
        masterNodeInfo = master;
    }

    /**
     * Checks if an external node provider is available.
     *
     * @return true if an external node provider exists, false otherwise
     */
    public boolean isExternalProviderAvailable() {
        return this.externalNodeProvider != null;
    }

    /**
     * Sets the local node provider responsible for managing nodes in the topology.
     * Initializes the master node description if it has not already been set, updates the master node
     * information, and configures the master node in the Docker manager.
     *
     * @param provider the {@code NodeProvider} instance to be set as the local node provider
     */
    public void setLocalNodeProvider(NodeProvider provider) {
        this.localNodeProvider = provider;
        // Set the auto-determined master node info
        // initialize the hostname and ip address in the master node description
        if (masterNodeInfo == null) {
            initMasterNodeDescription();
        }
        setMasterNodeInfo();
        DockerManager.setMasterNode(masterNodeInfo);
    }

    /**
     * Sets the volatile instance provider responsible for managing volatile instances
     * in the topology. The volatile instance provider is used to handle operations
     * such as retrieval and deletion of volatile instances.
     *
     * @param provider the {@code VolatileInstanceProvider} instance to be set as the volatile instance provider
     */
    public void setVolatileInstanceProvider(VolatileInstanceProvider provider) {
        this.volatileInstanceProvider = provider;
    }

    /**
     * Registers a {@link NodeOperationListener} to receive notifications about
     * node or tool installation operations within the topology.
     *
     * @param listener the {@code NodeOperationListener} instance to be registered
     */
    public void registerListener(NodeOperationListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Unregisters a {@link NodeOperationListener} to stop receiving notifications
     * about node or tool installation operations within the topology.
     *
     * @param listener the {@code NodeOperationListener} instance to be unregistered
     */
    public void unregisterListener(NodeOperationListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Retrieves the list of node flavors available for the topology. This includes
     * flavors from the local provider and, if available, flavors from the external
     * provider. If no flavors are found in the local provider, the external provider
     * flavors will be added as a fallback.
     *
     * @return a list of {@code NodeFlavor} objects representing the available node flavors,
     *         combining local and external provider data when necessary. If neither
     *         provider has any flavors, returns an empty list.
     */
    public List<NodeFlavor> listNodeFlavors() {
        List<NodeFlavor> flavors = this.localNodeProvider.listFlavors();
        if (flavors == null) {
            flavors = new ArrayList<>();
        }
        if (flavors.size() <= 1 && this.externalNodeProvider != null) {
            flavors.addAll(this.externalNodeProvider.listFlavors());
        }
        return flavors;
    }

    /**
     * Retrieves the details of a node based on the specified host name.
     *
     * @param hostName the name of the host for which the node details need to be retrieved
     * @return the {@code NodeDescription} object containing details of the node associated with the given host name
     */
    public NodeDescription getNode(String hostName) { return this.localNodeProvider.getNode(hostName); }

    /**
     * Retrieves the list of nodes managed by the local node provider.
     * The returned list contains active nodes registered in the topology.
     *
     * @return a list of {@code NodeDescription} objects representing the active nodes managed by the local node provider
     */
    public List<NodeDescription> listNodes() {
        /*return this.externalNodeProvider != null ?
                this.externalNodeProvider.listNodes() : this.localNodeProvider.listNodes();*/
        return this.localNodeProvider.listNodes();
    }

    /**
     * Adds a new node to the topology, initializes its configuration, and optionally installs
     * required tools based on the node's description. The method interacts with external and
     * local providers, installs services, and notifies relevant listeners about the operation.
     * It also manages processes like creating working directories and handling volatile nodes.
     *
     * @param info the {@code NodeDescription} object containing information about the node to be added,
     *             such as its ID, flavor, and owner details
     * @return the updated {@code NodeDescription} object after being processed and added
     *         to the topology
     * @throws TopologyException if there is an error while adding the node to the topology
     */
    public NodeDescription addNode(NodeDescription info) throws TopologyException {
        final ConfigurationProvider cfgProvider = ConfigurationManager.getInstance();
        if (this.externalNodeProvider != null) {
            info = this.externalNodeProvider.create(info);
            if (Boolean.TRUE.equals(info.getVolatile())) {
                VolatileInstance vi = new VolatileInstance();
                vi.setNodeId(info.getId());
                vi.setFlavorId(info.getFlavor().getId());
                vi.setUserId(info.getOwner());
                vi.setCreated(LocalDateTime.now());
                try {
                    this.volatileInstanceProvider.save(vi);
                } catch (PersistenceException e) {
                    logger.severe(e.getMessage());
                }
            }
            String device;
            if (Integer.parseInt(cfgProvider.getValue("openstack.volume.ssd.size", "100")) > 0) {
                device = cfgProvider.getValue("openstack.volume.ssd.device", "/dev/sds");
                final ServiceInstallStatus status = createWorkingDir(info, device);
                if (status.getStatus() != ServiceStatus.INSTALLED) {
                    logger.severe(String.format("Device %s has not been mount on node %s!", device, info.getId()));
                }
            } else if (Integer.parseInt(cfgProvider.getValue("openstack.volume.hdd.size", "0")) > 0) {
                device = cfgProvider.getValue("openstack.volume.hdd.device", "/dev/sdh");
                final ServiceInstallStatus status = createWorkingDir(info, device);
                if (status.getStatus() != ServiceStatus.INSTALLED) {
                    logger.severe(String.format("Device %s has not been mount on node %s!", device, info.getId()));
                }
            }
        }
        info.setActive(false);
        this.localNodeProvider.create(info);
        notifyListeners(info, ServiceInstallStatus.NODE_ADDED);
        if (cfgProvider.getBooleanValue("openstack.execute.installers")) {
            for (TopologyToolInstaller installer : installers) {
                // execute all the installers
                // Make execution synchronous, otherwise other commands sent to this machine may fail
                //this.executorService.submit(new BinaryTask<NodeDescription, ServiceInstallStatus>(info, this::notifyListeners) {
                final BinaryTask<NodeDescription, ServiceInstallStatus> task = new BinaryTask<>(info, this::notifyListeners) {
                    @Override
                    public ServiceInstallStatus execute(NodeDescription node) {
                        ServiceInstallStatus status = installer.installNewNode(node);
                        try {
                            node.setActive(true);
                            node = localNodeProvider.update(node);
                        } catch (Exception e) {
                            logger.severe(String.format("Cannot update service status for '%s' on node [%s]",
                                                        status.getServiceName(), node.getId()));
                        }
                        return status;
                    }
                };
                task.run();
            }
        } else {
            info.setActive(true);
            localNodeProvider.update(info);
        }
        Message message = new Message();
        message.setTopic(Topic.TOPOLOGY.value());
        message.setUserId(SystemPrincipal.instance().getName());
        message.setPersistent(false);
        message.addItem("node", info.getId());
        message.addItem("operation", "added");
        message.addItem("user", info.getUserName());
        message.addItem("password", info.getUserPass());
        Messaging.send(SystemPrincipal.instance(), Topic.TOPOLOGY.value(), message);
        return info;
    }

    /**
     * Installs services on a specified node using the available topology tool installers.
     * It iterates through a list of installers and attempts to install the specified service on the node.
     * Results of the installation, including success or failure, are logged and communicated via messaging.
     *
     * @param node the {@code NodeDescription} representing the node where the service will be installed
     * @param service the {@code ServiceDescription} representing the service to be installed on the specified node
     */
    public void installServices(NodeDescription node, ServiceDescription service) {
        BinaryTask<NodeDescription, ServiceInstallStatus> task;
        for (TopologyToolInstaller installer: installers) {
            // execute all the installers
            task = new BinaryTask<>(node, this::notifyListeners) {
                @Override
                public ServiceInstallStatus execute(final NodeDescription node) {
                    ServiceInstallStatus status = null;
                    try {
                        status = installer.installNewNode(node);
                        String msg;
                        if (status == ServiceInstallStatus.NODE_ADDED) {
                            msg = String.format("%s installed on node %s", status.getServiceName(), node.getId());
                            logger.info(msg);
                        } else {
                            msg = String.format("%s installation failed on node %s", status.getServiceName(), node.getId());
                            logger.warning(msg);
                        }
                        Messaging.send(SystemPrincipal.instance().getName(), Topic.TOPOLOGY.value(), msg);
                    } catch (Exception e) {
                        logger.severe(e.getMessage());
                    }
                    return status;
                }
            };
            task.execute(node);
        }
    }

    /**
     * Removes a node from the topology based on the specified host name.
     * This method identifies the node through the local or external providers,
     * notifies listeners about the removal, and handles cleanup of related resources.
     *
     * @param hostName the name of the host whose node is to be removed from the topology
     * @throws TopologyException if the node with the specified host name does not exist
     */
    public void removeNode(String hostName) {
        final NodeDescription node = getNode(hostName);
        if (node == null) {
            throw new TopologyException(String.format("Node [%s] doesn't exist", hostName));
        }
        if (this.externalNodeProvider != null/* && node.getVolatile()*/) {
            final String serverId = node.getServerId();
            this.externalNodeProvider.remove(serverId != null ? serverId : hostName);
            if (this.volatileInstanceProvider != null) {
                try {
                    this.volatileInstanceProvider.deleteByNode(node.getId());
                } catch (PersistenceException e) {
                    logger.severe(e.getMessage());
                }
            }
        } else {
            // execute all the installers
            for (TopologyToolInstaller installer: installers) {
                this.executorService.submit(new BinaryTask<NodeDescription, ServiceInstallStatus>(node, this::notifyListeners) {
                    @Override
                    public ServiceInstallStatus execute(NodeDescription ref) { return installer.uninstallNode(node); }
                });
            }
        }
        this.localNodeProvider.remove(hostName);
        notifyListeners(node, ServiceInstallStatus.NODE_REMOVED);
        Message message = new Message();
        message.setTopic(Topic.TOPOLOGY.value());
        message.setUserId(SystemPrincipal.instance().getName());
        message.setPersistent(false);
        message.addItem("node", hostName);
        message.addItem("operation", "removed");
        Messaging.send(SystemPrincipal.instance(), Topic.TOPOLOGY.value(), message);
    }

    /**
     * Updates the details of a specified node in the topology.
     * It processes the node description through available topology tool installers
     * and updates the node using external and local providers if applicable.
     *
     * @param nodeInfo the {@code NodeDescription} object containing the details of the
     *                 node to be updated. This includes information such as ID,
     *                 configurations, and other properties to be modified.
     */
    public void updateNode(NodeDescription nodeInfo) {
        //checkMount(nodeInfo);
        // execute all the installers
        for (TopologyToolInstaller installer: installers) {
            installer.editNode(nodeInfo);
        }
        if (this.externalNodeProvider != null) {
            nodeInfo = this.externalNodeProvider.update(nodeInfo);
        }
        this.localNodeProvider.update(nodeInfo);
    }

    /**
     * Retrieves the information about the master node in the topology.
     *
     * @return the {@code NodeDescription} object representing the master node
     */
    public NodeDescription getMasterNodeInfo() {
        return masterNodeInfo;
    }

    /**
     * Configures the master node information for all the topology tool installers associated
     * with this instance of the TopologyManager. This method iterates through the list of
     * registered installers and updates their master node descriptions using the master
     * node information stored in the current instance.
     */
    public void setMasterNodeInfo() {
        for (TopologyToolInstaller installer: this.installers) {
            installer.setMasterNodeDescription(masterNodeInfo);
        }
    }

    /**
     * Retrieves the list of Docker image installers available through the service registry.
     * These installers are used to manage Docker images and their installation process.
     *
     * @return a list of {@code DockerImageInstaller} instances representing the available installers.
     *         If no installers are found in the service registry, returns an empty list.
     */
    public List<DockerImageInstaller> getInstallers() {
        ServiceRegistry<DockerImageInstaller> serviceRegistry =
                ServiceRegistryManager.getInstance().getServiceRegistry(DockerImageInstaller.class);
        return new ArrayList<>(serviceRegistry.getServices());
    }

    /**
     * Retrieves a list of all standalone containers available in the current topology.
     * Standalone containers correspond to Docker plugins registered as {@code SingletonContainer}
     * in the service registry.
     *
     * @return a list of {@code SingletonContainer} instances representing standalone containers.
     *         If no containers are found, an empty list is returned.
     */
    public List<SingletonContainer> getStandaloneContainers() {
        ServiceRegistry<SingletonContainer> serviceRegistry =
                ServiceRegistryManager.getInstance().getServiceRegistry(SingletonContainer.class);
        return new ArrayList<>(serviceRegistry.getServices());
    }

    /**
     * Notifies all registered {@link NodeOperationListener} instances about the completion
     * of an operation related to a specific node and its service installation status.
     *
     * @param node the {@code NodeDescription} representing the node for which the operation was completed
     * @param installStatus the {@code ServiceInstallStatus} indicating the result of the service installation
     *                       on the specified node
     */
    public void notifyListeners(NodeDescription node, ServiceInstallStatus installStatus) {
        for (NodeOperationListener listener : this.listeners) {
            listener.onCompleted(node, installStatus);
        }
    }

    /**
     * Checks the master share configuration and attempts to ensure it is set up correctly.
     * Depending on the operating system, it may execute commands to create a local share
     * or verify the share status. If an issue occurs during setup, details are logged,
     * and the status is updated accordingly.
     *
     * @return a {@code ServiceInstallStatus} object representing the status of the master share setup.
     *         The status includes the name of the service, whether it is installed or encountered an error,
     *         and a reason if an error occurred.
     */
    public ServiceInstallStatus checkMasterShare() {
        ServiceInstallStatus status = new ServiceInstallStatus();
        status.setServiceName("Local Share");
        //String path = ExecutionConfiguration.getMasterContainerVolumeMap().getHostWorkspaceFolder();
        String share = ConfigurationManager.getInstance().getValue("master.share.name", "/tao");
        status.setStatus(ServiceStatus.INSTALLED);
        if (!(share.startsWith("\\\\") || share.startsWith("//"))) {
            List<String> commands;
            String shareName = share.startsWith("/") ? share.substring(1) : share;
            if (SystemUtils.IS_OS_WINDOWS) {
                commands = ProcessHelper.tokenizeCommands(createLocalShareWindowsTemplate, shareName, Paths.get(SystemVariable.ROOT.value()).toAbsolutePath());
            } else {
                commands = ProcessHelper.tokenizeCommands(createLocalShareLinuxTemplate, shareName, SystemVariable.ROOT.value());
            }
            ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                                  masterNodeInfo.getId(),
                                                  masterNodeInfo.getUserName(),
                                                  masterNodeInfo.getUserPass(),
                                                  commands, true, SSHMode.EXEC);
            sharedAccumulator.reset();
            Executor<?> executor = Executor.execute(sharedAccumulator, job);
            logger.fine("Executing " + String.join(" ", commands));
            waitFor(executor, 5, TimeUnit.SECONDS);
            int retCode;
            if ((retCode = executor.getReturnCode()) != 0) {
                status.setStatus(ServiceStatus.ERROR);
                status.setReason(sharedAccumulator.getOutput());
                logger.warning(String.format("Command %s failed with code %d", String.join(" ", commands), retCode));
            }
            sharedAccumulator.reset();
        }
        return status;
    }

    /**
     * Creates a working directory on the specified node using the provided device information.
     * Executes the necessary commands to set up the directory via SSH.
     *
     * @param node the {@link NodeDescription} object containing the details of the node where the working directory is to be created
     * @param device the device identifier for use in the directory creation commands
     * @return a {@link ServiceInstallStatus} object representing the status of the operation,
     *         including whether the operation was successful or encountered an error
     */
    public ServiceInstallStatus createWorkingDir(NodeDescription node, String device) {
        ServiceInstallStatus status = new ServiceInstallStatus();
        status.setServiceName("Worker workdir");
        List<String> commands = ProcessHelper.tokenizeCommands(createWorkingDirectoryTemplate, device);
        ExecutionUnit job = new ExecutionUnit(ExecutorType.SSH2,
                                              node.getId(),
                                              node.getUserName(),
                                              node.getSshKey() != null ? node.getSshKey() : node.getUserPass(),
                                              commands, true, SSHMode.EXEC);
        if(node.getSshKey() != null) {
            job.setCertificate(node.getSshKey());
        }
        sharedAccumulator.reset();
        Executor<?> executor = Executor.execute(sharedAccumulator, job);
        logger.fine("Executing " + String.join(" ", commands));
        waitFor(executor, 5, TimeUnit.SECONDS);
        int retCode;
        if ((retCode = executor.getReturnCode()) != 0) {
            status.setStatus(ServiceStatus.ERROR);
            status.setReason(sharedAccumulator.getOutput());
            logger.warning(String.format("Command %s failed with code %d", String.join(" ", commands), retCode));
        } else {
            status.setStatus(ServiceStatus.INSTALLED);
        }
        sharedAccumulator.reset();
        return status;
    }

    private void initMasterNodeDescription() throws TopologyException {
        try {
            // TODO: Aparently, the hostname obtained by this method might return a different value
            // than the call to "hostname" call in Linux. Maybe an invocation of hostname will solve the problem
            // but this might break the portability
            String hostName = InetAddress.getLocalHost().getHostName();
            masterNodeInfo = this.localNodeProvider.getNode(hostName);
            if (masterNodeInfo == null) {
                masterNodeInfo = new NodeDescription();
                masterNodeInfo.setId(hostName);
            }
        } catch (Exception e) {
            throw new TopologyException("Master hostname retrieval failure", e);
        }
    }

    private void waitFor(Executor<?> executor, long amount, TimeUnit unit) {
        try {
            executor.getWaitObject().await(amount, unit);
        } catch (InterruptedException e) {
            logger.warning("Process timed out: " + e.getMessage());
        }
    }
}
