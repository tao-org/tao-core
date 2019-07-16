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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.docker.Container;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Topics;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.spi.ServiceLoader;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.topology.docker.DockerImageInstaller;
import ro.cs.tao.utils.async.BinaryTask;
import ro.cs.tao.utils.async.LazyInitialize;
import ro.cs.tao.utils.executors.*;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Cosmin Udroiu
 */
public class TopologyManager implements ITopologyManager {

    private static final String dockerBuildCmdTemplate;
    private static final String dockerTagCmdTemplate;
    private static final String dockerPushCmdTemplate;
    private static final String dockerListCmdTemplate;
    private static final String dockerListAllCmd;
    private static final String createMountTemplate;
    private static final String createLocalShareWindowsTemplate;
    private static final String createLocalShareLinuxTemplate;
    private static final OutputAccumulator sharedAccumulator;
    private static final TopologyManager instance;

    private final Logger logger;
    private NodeDescription masterNodeInfo;
    private final Set<TopologyToolInstaller> installers;
    private final Supplier<PersistenceManager> persistenceManager;
    private final ExecutorService executorService;

    static {
        dockerBuildCmdTemplate = "docker build -t %s %s";
        dockerTagCmdTemplate = "docker tag %s %s";
        dockerPushCmdTemplate = "docker push %s";
        dockerListAllCmd = "docker images --format {{.ID}}\\t{{.Tag}}\\t{{.Repository}}";
        dockerListCmdTemplate = "docker images %s --format {{.ID}}\\t{{.Tag}}\\t{{.Repository}}";

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
            return getPersistenceManager().getNodeByHostName(hostName);
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
        return getPersistenceManager().getNodes().stream()
                .filter(NodeDescription::getActive)     // we take only active node but not the deleted ones
                .collect(Collectors.toList());
    }

    @Override
    public void add(NodeDescription info) throws TopologyException {
        if (getPersistenceManager().checkIfExistsNodeByHostName(info.getId())) {
            try {
                info = getPersistenceManager().updateExecutionNode(info);
            } catch (PersistenceException e) {
                logger.severe("Cannot update node description to database. Rolling back installation on node " + info.getId() + "...");
                throw new TopologyException(e);
            }
        } else {
            try {
                info = getPersistenceManager().saveExecutionNode(info);
            } catch (PersistenceException e) {
                logger.severe("Cannot save node description to database. Rolling back installation on node " + info.getId() + "...");
                throw new TopologyException(e);
            }
        }
        // make sure the common share is mounted on the node
        onCompleted(info, checkMount(info));
        for (TopologyToolInstaller installer: installers) {
            // execute all the installers
            this.executorService.submit(new BinaryTask<NodeDescription, ToolInstallStatus>(info, this::onCompleted) {
                @Override
                public ToolInstallStatus execute(NodeDescription node) {
                    ToolInstallStatus status = installer.installNewNode(node);
                    try {
                        node = getPersistenceManager().updateExecutionNode(node);
                    } catch (PersistenceException e) {
                        logger.severe(String.format("Cannot update service status for '%s' on node [%s]",
                                                    status.getToolName(), node.getId()));
                    }
                    return status;
                }
            });
        }
        Message message = new Message();
        message.setTopic(Topics.TOPOLOGY);
        message.setUser(SystemPrincipal.instance().getName());
        message.setPersistent(false);
        message.addItem("node", info.getId());
        message.addItem("operation", "added");
        message.addItem("user", info.getUserName());
        message.addItem("password", info.getUserPass());
        Messaging.send(SystemPrincipal.instance(), Topics.TOPOLOGY, message);
    }

    @Override
    public void remove(String hostName) {
        NodeDescription node = get(hostName);
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
            this.executorService.submit(new BinaryTask<NodeDescription, ToolInstallStatus>(node, this::onCompleted) {
                @Override
                public ToolInstallStatus execute(NodeDescription ref) { return installer.uninstallNode(node); }
            });
        }
        Message message = new Message();
        message.setTopic(Topics.TOPOLOGY);
        message.setUser(SystemPrincipal.instance().getName());
        message.setPersistent(false);
        message.addItem("node", hostName);
        message.addItem("operation", "removed");
        Messaging.send(SystemPrincipal.instance(), Topics.TOPOLOGY, message);
    }

    @Override
    public void update(NodeDescription nodeInfo) {
        checkMount(nodeInfo);
        // execute all the installers
        for (TopologyToolInstaller installer: installers) {
            installer.editNode(nodeInfo);
        }
        try {
            getPersistenceManager().updateExecutionNode(nodeInfo);
        } catch (PersistenceException e) {
            logger.severe("Cannot update node description in database for the host name:" + nodeInfo.getId());
            throw new TopologyException(e);
        }
    }

    public NodeDescription getMasterNodeInfo() {
        return this.masterNodeInfo;
    }

    public void setMasterNodeInfo(NodeDescription masterNodeInfo) {
        this.masterNodeInfo = masterNodeInfo;
        for (TopologyToolInstaller installer: this.installers) {
            installer.setMasterNodeDescription(masterNodeInfo);
        }
        Map<String, String> settings = ConfigurationManager.getInstance().getValues("topology.master");
        this.masterNodeInfo.setUserName(settings.get("topology.master.user"));
        this.masterNodeInfo.setUserPass(settings.get("topology.master.password"));
    }

    @Override
    public String registerImage(Path imagePath, String shortName, String description) throws TopologyException {
        if (imagePath == null || !Files.exists(imagePath)) {
            throw new TopologyException("Invalid image path");
        }
        Principal principal = SessionStore.currentContext() != null ?
                SessionStore.currentContext().getPrincipal() : SystemPrincipal.instance();
        String correctedName = shortName.replace(" ", "-");
        List<String> commands = ProcessHelper.tokenizeCommands(dockerBuildCmdTemplate,
                                                               correctedName, imagePath.getParent().toString());
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNodeInfo.getId(),
                                              masterNodeInfo.getUserName(),
                                              masterNodeInfo.getUserPass(),
                                              commands, false, SSHMode.EXEC);
        sharedAccumulator.reset();
        int retCode;
        Executor executor = Executor.execute(sharedAccumulator, job);
        logger.fine("Executing " + String.join(" ", commands));
        String containerId = null;
        waitFor(executor, 10, TimeUnit.MINUTES);
        if ((retCode = executor.getReturnCode()) == 0) {
            Container image = getDockerImage(correctedName);
            containerId = image.getId();
            String localRegistry = ConfigurationManager.getInstance().getValue("tao.docker.registry");
            String tag = localRegistry + "/" + correctedName;
            commands = ProcessHelper.tokenizeCommands(dockerTagCmdTemplate, containerId, tag);
            job = new ExecutionUnit(ExecutorType.PROCESS, masterNodeInfo.getId(),
                                    masterNodeInfo.getUserName(), masterNodeInfo.getUserPass(),
                                    commands, false, SSHMode.EXEC);
            sharedAccumulator.reset();
            executor = Executor.execute(sharedAccumulator, job);
            logger.fine("Executing " + String.join(" ", commands));
            waitFor(executor, 30, TimeUnit.SECONDS);
            if ((retCode = executor.getReturnCode()) == 0) {
                if (!SystemUtils.IS_OS_WINDOWS) {
                    commands = ProcessHelper.tokenizeCommands(dockerPushCmdTemplate, tag);
                    job = new ExecutionUnit(ExecutorType.PROCESS, masterNodeInfo.getId(),
                                            masterNodeInfo.getUserName(), masterNodeInfo.getUserPass(),
                                            commands, false, SSHMode.EXEC);
                    sharedAccumulator.reset();
                    executor = Executor.execute(sharedAccumulator, job);
                    logger.fine("Executing " + String.join(" ", commands));
                    waitFor(executor, 10, TimeUnit.MINUTES);
                    if ((retCode = executor.getReturnCode()) == 0) {
                        Messaging.send(principal, Topics.INFORMATION, this,
                                       String.format("Docker image '%s' successfully registered", correctedName));
                        return containerId;
                    } else {
                        logger.severe("Command output: " + sharedAccumulator.getOutput());
                    }
                } else {
                    Messaging.send(principal, Topics.INFORMATION, this,
                                   String.format("Docker image '%s' successfully registered", correctedName));
                    return containerId;
                }
            } else {
                logger.severe(String.format("Execution failed with code %s. Command output: %s",
                                            executor.getReturnCode(),
                                            sharedAccumulator.getOutput()));
            }
        }
        String message = String.format("Registration for image '%s' failed with code %s. Details: '%s'",
                                       correctedName, retCode, sharedAccumulator.getOutput());
        sharedAccumulator.reset();
        logger.severe(message);
        Messaging.send(principal, Topics.ERROR, this, message);
        return containerId;
    }

    @Override
    public List<Container> getAvailableDockerImages() {
        List<Container> containers = getDockerImages();
        List<Container> dbContainers = getPersistenceManager().getContainers();
        if (containers.size() == 0) {
            logger.warning("Docker execution failed. Check that Docker is installed and the sudo credentials are valid");
        } else {
            dbContainers.retainAll(containers);
        }
        return dbContainers;
    }

    @Override
    public List<DockerImageInstaller> getInstallers() {
        ServiceRegistry<DockerImageInstaller> serviceRegistry =
                ServiceRegistryManager.getInstance().getServiceRegistry(DockerImageInstaller.class);
        return new ArrayList<>(serviceRegistry.getServices());
    }

    public Container getDockerImage(String name) {
        Container container = null;
        final List<String> commands = ProcessHelper.tokenizeCommands(dockerListCmdTemplate, name);
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNodeInfo.getId(),
                                              masterNodeInfo.getUserName(),
                                              masterNodeInfo.getUserPass(),
                                              commands, false, SSHMode.EXEC);
        sharedAccumulator.reset();
        int retCode;
        final Executor executor = Executor.execute(sharedAccumulator, job);
        waitFor(executor, 50, TimeUnit.SECONDS);
        if ((retCode = executor.getReturnCode()) == 0) {
            String[] lines = sharedAccumulator.getOutput().split("\n");
            for (String line : lines) {
                String[] tokens = line.split("[ \t]");
                List<String> list = Arrays.stream(tokens).filter(item -> !item.trim().isEmpty()).
                        map(item -> StringUtils.strip(item, "'")).
                        collect(Collectors.toList());
                if (list.size() > 2) {
                    // we might have two formats for the response
                    String containerId = list.get(0);
                    if (!"IMAGE_ID".equals(containerId) && !"REPOSITORY".equals(containerId)) {
                        container = new Container();
                        container.setId(containerId);
                        container.setName(list.get(2));
                        container.setTag(list.get(1));
                    }
                }
            }
        } else {
            String message = String.format("Docker command failed wit code %s. Details: '%s'",
                                           retCode, sharedAccumulator.getOutput());
            sharedAccumulator.reset();
            logger.severe(message);
        }

        return container;
    }

    private ToolInstallStatus checkMount(NodeDescription node) {
        ToolInstallStatus status = new ToolInstallStatus();
        status.setToolName("Mount");
        String mount = ConfigurationManager.getInstance().getValue("node.mount.folder", "/mnt/tao");
        String share = ConfigurationManager.getInstance().getValue("master.share.name", "/tao");
        final List<String> commands = ProcessHelper.tokenizeCommands(createMountTemplate, mount, masterNodeInfo.getId(),
                                                                     share, masterNodeInfo.getUserName(), masterNodeInfo.getUserPass());
        ExecutionUnit job = new ExecutionUnit(ExecutorType.SSH2,
                                              node.getId(),
                                              node.getUserName(),
                                              node.getUserPass(),
                                              commands, true, SSHMode.EXEC);
        sharedAccumulator.reset();
        Executor executor = Executor.execute(sharedAccumulator, job);
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

    public ToolInstallStatus checkShare(NodeDescription master) {
        ToolInstallStatus status = new ToolInstallStatus();
        status.setToolName("Local Share");
        String path = ConfigurationManager.getInstance().getValue("workspace.location", "/mnt/tao/working_dir");
        String share = ConfigurationManager.getInstance().getValue("master.share.name", "/tao");
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
                                                  master.getId(),
                                                  master.getUserName(),
                                                  master.getUserPass(),
                                                  commands, true, SSHMode.EXEC);
            sharedAccumulator.reset();
            Executor executor = Executor.execute(sharedAccumulator, job);
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

    private void waitFor(Executor executor, long amount, TimeUnit unit) {
        try {
            executor.getWaitObject().await(amount, unit);
        } catch (InterruptedException e) {
            logger.warning("Process timed out: " + e.getMessage());
        }
    }

    private List<Container> getDockerImages() {
        List<Container> containers = new ArrayList<>();
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNodeInfo.getId(),
                                              masterNodeInfo.getUserName(),
                                              masterNodeInfo.getUserPass(),
                                              ProcessHelper.tokenizeCommands(dockerListAllCmd),
                                              false, SSHMode.EXEC);
        sharedAccumulator.reset();
        sharedAccumulator.preserveLineSeparator(true);
        final Executor executor = Executor.execute(sharedAccumulator, job);
        waitFor(executor, 3, TimeUnit.SECONDS);
        if (executor.getReturnCode() == 0) {
            final String output = sharedAccumulator.getOutput();
            String[] lines = SystemUtils.IS_OS_WINDOWS ? output.split("\n") : output.replace("\n", "").split(";");
            for (String line : lines) {
                String[] tokens = line.split(" |\t");
                List<String> list = Arrays.stream(tokens).filter(item -> !item.trim().isEmpty()).
                        map(item -> StringUtils.strip(item, "'")).
                        collect(Collectors.toList());
                if (list.size() > 2) {
                    // we might have two formats for the response
                    String containerId = list.get(0);
                    if (!"IMAGE_ID".equals(containerId) && !"REPOSITORY".equals(containerId)) {
                        Container container = new Container();
                        container.setId(containerId);
                        container.setName(list.get(2));
                        container.setTag(list.get(1));
                        containers.add(container);
                        //getPersistenceManager().getContainerById(containerId);
                    }
                }
            }
        } else {
            String message = String.format("Docker command failed. Details: '%s'", sharedAccumulator.getOutput());
            sharedAccumulator.reset();
            logger.severe(message);
        }
        sharedAccumulator.preserveLineSeparator(false);
        return containers;
    }

    public void onCompleted(NodeDescription node, ToolInstallStatus status) {
        switch (status.getStatus()) {
            case INSTALLED:
                Messaging.send(SystemPrincipal.instance(), Topics.INFORMATION,
                               this,
                               String.format("%s installation on %s completed",
                                              status.getToolName(),
                                              node.getId()));
                break;
            case UNINSTALLED:
                Messaging.send(SystemPrincipal.instance(), Topics.INFORMATION,
                                       this,
                                       String.format("%s uninstallation on %s completed",
                                              status.getToolName(),
                                              node.getId()));
                break;
            case ERROR:
                Messaging.send(SystemPrincipal.instance(), Topics.WARNING,
                                       this,
                                       String.format("%s installation on %s failed [reason: %s]",
                                              status.getToolName(),
                                              node.getId(),
                                              status.getReason()));
                break;
        }
    }

    private void initMasterNodeDescription() throws TopologyException {
        try {
            // TODO: Aparently, the hostname obtained by this method might return a different value
            // than the call to "hostname" call in Linux. Maybe an invocation of hostname will solve the problem
            // but this might break the portability
            String hostName = InetAddress.getLocalHost().getHostName();
            this.masterNodeInfo = getPersistenceManager().getNodeByHostName(hostName);
            if (this.masterNodeInfo == null) {
                masterNodeInfo = new NodeDescription();
                masterNodeInfo.setId(hostName);
                Map<String, String> settings = ConfigurationManager.getInstance().getValues("topology.master");
                masterNodeInfo.setUserName(settings.get("topology.master.user"));
                masterNodeInfo.setUserPass(settings.get("topology.master.password"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new TopologyException("Master hostname retrieval failure", e);
        }
    }

    private PersistenceManager getPersistenceManager() {
        return this.persistenceManager.get();
    }
}
