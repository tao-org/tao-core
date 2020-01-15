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
import ro.cs.tao.configuration.Configuration;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Topic;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.topology.docker.DockerImageInstaller;
import ro.cs.tao.topology.docker.DockerManager;
import ro.cs.tao.topology.provider.DefaultNodeOperationListener;
import ro.cs.tao.topology.provider.DefaultNodeProvider;
import ro.cs.tao.topology.provider.NodeOperationListener;
import ro.cs.tao.topology.provider.NodeProvider;
import ro.cs.tao.utils.async.BinaryTask;
import ro.cs.tao.utils.async.LazyInitialize;
import ro.cs.tao.utils.executors.*;

import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * @author Cosmin Udroiu
 */
public class TopologyManager {

    private static final String createMountTemplate;
    private static final String createLocalShareWindowsTemplate;
    private static final String createLocalShareLinuxTemplate;
    private static final OutputAccumulator sharedAccumulator;
    private static final TopologyManager instance;
    private static NodeDescription masterNodeInfo;

    private final Logger logger;
    private NodeProvider nodeProvider;
    private final Set<NodeOperationListener> listeners;
    private final Set<TopologyToolInstaller> installers;
    private final Supplier<PersistenceManager> persistenceManager;
    private final ExecutorService executorService;

    static {
        //if [ ! -e /mnt/tao ]; then sed -i '$a//#MASTERHOST##SHARE# /mnt/tao cifs user=tao,password=tao123,file_mode=0777,dir_mode=0777,noperm 0 0' /etc/fstab; fi
        createMountTemplate = "if [ ! -e %1$s ]; then mkdir %1$s; chmod 777 %1$s; sed -i \"$ a//%2$s%3$s %1$s cifs user=%4,password=%5,file_mode=0777,dir_mode=0777,noperm 0 0\" /etc/fstab; mount -a; fi;";
        createLocalShareWindowsTemplate = "net share %s=%s /GRANT:Everyone,FULL";
        createLocalShareLinuxTemplate = "if [ -e $(smbclient -N -g -L localhost | grep \"%1$s\") ]; then sed -i \"$ a[%1$s]\n\tpath = %2$s\n\tbrowsable = yes\n\twritable = yes\n\tguest ok = yes\" /etc/samba/smb.conf; fi;";
        sharedAccumulator = new OutputAccumulator();
        instance = new TopologyManager();

    }

    private TopologyManager() {
        this.logger = Logger.getLogger(TopologyManager.class.getName());
        this.persistenceManager = LazyInitialize.using(() -> SpringContextBridge.services().getService(PersistenceManager.class));
        this.executorService = new NamedThreadPoolExecutor("topology-thread", 1);//Executors.newSingleThreadExecutor();
        // initialize the hostname and ip address in the master node description
        if (masterNodeInfo == null) {
            initMasterNodeDescription();
        }

        // load all services for tool installers and initialize the master node info
        ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        final ServiceRegistry<TopologyToolInstaller> installersRegistry = serviceRegistryManager.getServiceRegistry(TopologyToolInstaller.class);
        this.installers = installersRegistry.getServices();
        final ServiceRegistry<NodeProvider> nodeProviderRegistry = serviceRegistryManager.getServiceRegistry(NodeProvider.class);
        final String nodeProviderClassName = ConfigurationManager.getInstance().getValue(Configuration.Topology.PROVIDER_CLASS,
                                                                                         DefaultNodeProvider.class.getName());
        final Set<NodeProvider> nodeProviders = nodeProviderRegistry.getServices();
        if (nodeProviders == null) {
            throw new TopologyException("No instances of NodeProvider found");
        }
        for (NodeProvider provider : nodeProviders) {
            if (provider.getClass().getName().equals(nodeProviderClassName)) {
                this.nodeProvider = provider;
                break;
            }
        }
        if (this.nodeProvider == null) {
            throw new TopologyException(String.format("NodeProvider [%s] not found", nodeProviderClassName));
        }
        this.listeners = new HashSet<>();
        registerListener(new DefaultNodeOperationListener());
        // Set the auto-determined master node info
        setMasterNodeInfo();
        DockerManager.setMasterNode(masterNodeInfo);
    }

    public static TopologyManager getInstance() {
        return instance;
    }

    public static void setMasterNode(NodeDescription master) {
        masterNodeInfo = master;
    }

    public void registerListener(NodeOperationListener listener) {
        this.listeners.add(listener);
    }

    public void unregisterListener(NodeOperationListener listener) {
        this.listeners.remove(listener);
    }

    public NodeDescription getNode(String hostName) {
        return this.nodeProvider.getNode(hostName);
    }

    public List<NodeDescription> listNodes() {
        return this.nodeProvider.listNodes();
    }

    public void addNode(NodeDescription info) throws TopologyException {
        this.nodeProvider.addNode(info);
        // make sure the common share is mounted on the node
        notifyListeners(info, checkMount(info));
        for (TopologyToolInstaller installer: installers) {
            // execute all the installers
            this.executorService.submit(new BinaryTask<NodeDescription, ServiceInstallStatus>(info, this::notifyListeners) {
                @Override
                public ServiceInstallStatus execute(NodeDescription node) {
                    ServiceInstallStatus status = installer.installNewNode(node);
                    try {
                        node = getPersistenceManager().updateExecutionNode(node);
                    } catch (PersistenceException e) {
                        logger.severe(String.format("Cannot update service status for '%s' on node [%s]",
                                                    status.getServiceName(), node.getId()));
                    }
                    return status;
                }
            });
        }
        Message message = new Message();
        message.setTopic(Topic.TOPOLOGY.value());
        message.setUser(SystemPrincipal.instance().getName());
        message.setPersistent(false);
        message.addItem("node", info.getId());
        message.addItem("operation", "added");
        message.addItem("user", info.getUserName());
        message.addItem("password", info.getUserPass());
        Messaging.send(SystemPrincipal.instance(), Topic.TOPOLOGY.value(), message);
    }

    public void removeNode(String hostName) {
        NodeDescription node = getNode(hostName);
        if (node == null) {
            throw new TopologyException(String.format("Node [%s] does not exist", hostName));
        }
        try {
            getPersistenceManager().deleteExecutionNode(hostName);
        } catch (PersistenceException e) {
            logger.severe("Cannot remove node description from database. Host name is :" + hostName);
            throw new TopologyException(e);
        }
        // execute all the installers
        for (TopologyToolInstaller installer: installers) {
            this.executorService.submit(new BinaryTask<NodeDescription, ServiceInstallStatus>(node, this::notifyListeners) {
                @Override
                public ServiceInstallStatus execute(NodeDescription ref) { return installer.uninstallNode(node); }
            });
        }
        Message message = new Message();
        message.setTopic(Topic.TOPOLOGY.value());
        message.setUser(SystemPrincipal.instance().getName());
        message.setPersistent(false);
        message.addItem("node", hostName);
        message.addItem("operation", "removed");
        Messaging.send(SystemPrincipal.instance(), Topic.TOPOLOGY.value(), message);
    }

    public void updateNode(NodeDescription nodeInfo) {
        checkMount(nodeInfo);
        // execute all the installers
        for (TopologyToolInstaller installer: installers) {
            installer.editNode(nodeInfo);
        }
        this.nodeProvider.updateNode(nodeInfo);
    }

    public NodeDescription getMasterNodeInfo() {
        return masterNodeInfo;
    }

    public void setMasterNodeInfo() {
        for (TopologyToolInstaller installer: this.installers) {
            installer.setMasterNodeDescription(masterNodeInfo);
        }
    }

    public List<DockerImageInstaller> getInstallers() {
        ServiceRegistry<DockerImageInstaller> serviceRegistry =
                ServiceRegistryManager.getInstance().getServiceRegistry(DockerImageInstaller.class);
        return new ArrayList<>(serviceRegistry.getServices());
    }

    public void notifyListeners(NodeDescription node, ServiceInstallStatus installStatus) {
        for (NodeOperationListener listener : this.listeners) {
            listener.onCompleted(node, installStatus);
        }
    }

    public ServiceInstallStatus checkMount(NodeDescription node) {
        ServiceInstallStatus status = new ServiceInstallStatus();
        status.setServiceName("Mount");
        String mount = ConfigurationManager.getInstance().getValue(Configuration.FileSystem.NODE_SHARE_MOUNT, "/mnt/tao");
        String share = ConfigurationManager.getInstance().getValue(Configuration.FileSystem.MASTER_SHARE, "/tao");
        final List<String> commands = ProcessHelper.tokenizeCommands(createMountTemplate, mount, masterNodeInfo.getId(),
                                                                     share, masterNodeInfo.getUserName(), masterNodeInfo.getUserPass());
        ExecutionUnit job = new ExecutionUnit(ExecutorType.SSH2,
                                              node.getId(),
                                              node.getUserName(),
                                              node.getUserPass(),
                                              commands, true, SSHMode.EXEC);
        sharedAccumulator.reset();
        Executor<?> executor = Executor.execute(sharedAccumulator, job);
        logger.fine("Executing " + String.join(" ", commands));
        waitFor(executor, 5, TimeUnit.SECONDS);
        if (executor.getReturnCode() == 0) {
            status.setStatus(ServiceStatus.INSTALLED);
        } else {
            status.setStatus(ServiceStatus.ERROR);
            status.setReason(sharedAccumulator.getOutput());
            logger.warning("Command failed: " + status.getReason());
        }
        sharedAccumulator.reset();
        return status;
    }

    public ServiceInstallStatus checkMasterShare() {
        ServiceInstallStatus status = new ServiceInstallStatus();
        status.setServiceName("Local Share");
        String path = ConfigurationManager.getInstance().getValue(Configuration.FileSystem.WORKSPACE_LOCATION, "/mnt/tao/working_dir");
        String share = ConfigurationManager.getInstance().getValue(Configuration.FileSystem.MASTER_SHARE, "/tao");
        status.setStatus(ServiceStatus.INSTALLED);
        if (!(share.startsWith("\\\\") || share.startsWith("//"))) {
            List<String> commands;
            String shareName = share.startsWith("/") ? share.substring(1) : share;
            if (SystemUtils.IS_OS_WINDOWS) {
                commands = ProcessHelper.tokenizeCommands(createLocalShareWindowsTemplate, shareName, Paths.get(path).toAbsolutePath());
            } else {
                commands = ProcessHelper.tokenizeCommands(createLocalShareLinuxTemplate, shareName, path);
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
            if (executor.getReturnCode() != 0) {
                status.setStatus(ServiceStatus.ERROR);
                status.setReason(sharedAccumulator.getOutput());
                logger.warning("Command failed: " + status.getReason());
            }
            sharedAccumulator.reset();
        }
        return status;
    }

    private void initMasterNodeDescription() throws TopologyException {
        try {
            // TODO: Aparently, the hostname obtained by this method might return a different value
            // than the call to "hostname" call in Linux. Maybe an invocation of hostname will solve the problem
            // but this might break the portability
            String hostName = InetAddress.getLocalHost().getHostName();
            masterNodeInfo = getPersistenceManager().getNodeByHostName(hostName);
            if (masterNodeInfo == null) {
                masterNodeInfo = new NodeDescription();
                masterNodeInfo.setId(hostName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new TopologyException("Master hostname retrieval failure", e);
        }
    }

    private PersistenceManager getPersistenceManager() {
        return this.persistenceManager.get();
    }

    private void waitFor(Executor<?> executor, long amount, TimeUnit unit) {
        try {
            executor.getWaitObject().await(amount, unit);
        } catch (InterruptedException e) {
            logger.warning("Process timed out: " + e.getMessage());
        }
    }
}
