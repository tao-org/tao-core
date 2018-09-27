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
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static final List<String> dockerBuildCmdTemplate;
    private static final List<String> dockerTagCmdTemplate;
    private static final List<String> dockerPushCmdTemplate;
    private static final List<String> dockerListCmdTemplate;
    private static final List<String> dockerListAllCmd;
    private static final OutputAccumulator sharedAccumulator;
    private static final TopologyManager instance;

    private final Logger logger;
    private NodeDescription masterNodeInfo;
    private final Set<TopologyToolInstaller> installers;
    private final Supplier<PersistenceManager> persistenceManager;
    private final ExecutorService executorService;

    static {
        dockerBuildCmdTemplate = new ArrayList<>(5);
        dockerBuildCmdTemplate.add("docker");
        dockerBuildCmdTemplate.add("build");
        dockerBuildCmdTemplate.add("-t");
        dockerBuildCmdTemplate.add("#NAME");
        dockerBuildCmdTemplate.add("#PATH");

        dockerTagCmdTemplate = new ArrayList<>(4);
        dockerTagCmdTemplate.add("docker");
        dockerTagCmdTemplate.add("tag");
        dockerTagCmdTemplate.add("#ID");
        dockerTagCmdTemplate.add("#TAG");

        dockerPushCmdTemplate = new ArrayList<>(3);
        dockerPushCmdTemplate.add("docker");
        dockerPushCmdTemplate.add("push");
        dockerPushCmdTemplate.add("#TAG");

        dockerListAllCmd = new ArrayList<>(4);
        dockerListAllCmd.add("docker");
        dockerListAllCmd.add("images");
        dockerListAllCmd.add("--format");
        dockerListAllCmd.add("{{.ID}}\\t{{.Tag}}\\t{{.Repository}};");

        dockerListCmdTemplate = new ArrayList<String>(5);
        dockerListCmdTemplate.add("docker");
        dockerListCmdTemplate.add("images");
        dockerListCmdTemplate.add("#NAME");
        dockerListCmdTemplate.add("--format");
        dockerListCmdTemplate.add("{{.ID}}\\t{{.Tag}}\\t{{.Repository}};");

        sharedAccumulator = new OutputAccumulator();

        instance = new TopologyManager();
    }

    private TopologyManager() {
        this.logger = Logger.getLogger(TopologyManager.class.getName());
        this.persistenceManager = LazyInitialize.using(() -> SpringContextBridge.services().getPersistenceManager());
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
                getPersistenceManager().updateExecutionNode(info);
            } catch (PersistenceException e) {
                logger.severe("Cannot update node description to database. Rolling back installation on node " + info.getId() + "...");
                throw new TopologyException(e);
            }
        } else {
            try {
                getPersistenceManager().saveExecutionNode(info);
            } catch (PersistenceException e) {
                logger.severe("Cannot save node description to database. Rolling back installation on node " + info.getId() + "...");
                throw new TopologyException(e);
            }
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
    }

    @Override
    public void update(NodeDescription nodeInfo) {
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
    public void registerImage(Path imagePath, String shortName, String description) throws TopologyException {
        if (imagePath == null || !Files.exists(imagePath)) {
            throw new TopologyException("Invalid image path");
        }
        Principal principal = SessionStore.currentContext() != null ?
                SessionStore.currentContext().getPrincipal() : SystemPrincipal.instance();
        String correctedName = shortName.replace(" ", "-");
        dockerBuildCmdTemplate.set(3, correctedName);
        dockerBuildCmdTemplate.set(4, imagePath.getParent().toString());
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNodeInfo.getId(),
                                              masterNodeInfo.getUserName(),
                                              masterNodeInfo.getUserPass(),
                                              dockerBuildCmdTemplate, false, SSHMode.EXEC);
        sharedAccumulator.reset();
        Executor executor = Executor.execute(sharedAccumulator, job);
        logger.fine("Executing " + String.join(" ", dockerBuildCmdTemplate));
        waitFor(executor, 5, TimeUnit.MINUTES);
        logger.fine("Execution of " + String.join(" ", dockerBuildCmdTemplate) + " returned code " + executor.getReturnCode());
        if (executor.getReturnCode() == 0) {
            Container image = getDockerImage(correctedName);
            String localRegistry = ConfigurationManager.getInstance().getValue("tao.docker.registry");
            String tag = localRegistry + "/" + correctedName;
            dockerTagCmdTemplate.set(2, image.getId());
            dockerTagCmdTemplate.set(3, tag);
            job = new ExecutionUnit(ExecutorType.PROCESS, masterNodeInfo.getId(),
                                    masterNodeInfo.getUserName(), masterNodeInfo.getUserPass(),
                                    dockerTagCmdTemplate, false, SSHMode.EXEC);
            sharedAccumulator.reset();
            executor = Executor.execute(sharedAccumulator, job);
            logger.fine("Executing " + String.join(" ", dockerTagCmdTemplate));
            waitFor(executor, 3, TimeUnit.SECONDS);
            logger.fine("Execution of " + String.join(" ", dockerTagCmdTemplate) + " returned code " + executor.getReturnCode());
            if (executor.getReturnCode() == 0) {
                if (!SystemUtils.IS_OS_WINDOWS) {
                    dockerPushCmdTemplate.set(2, tag);
                    job = new ExecutionUnit(ExecutorType.PROCESS, masterNodeInfo.getId(),
                                            masterNodeInfo.getUserName(), masterNodeInfo.getUserPass(),
                                            dockerPushCmdTemplate, false, SSHMode.EXEC);
                    sharedAccumulator.reset();
                    executor = Executor.execute(sharedAccumulator, job);
                    logger.fine("Executing " + String.join(" ", dockerPushCmdTemplate));
                    waitFor(executor, 30, TimeUnit.SECONDS);
                    logger.fine("Execution of " + String.join(" ", dockerPushCmdTemplate) + " returned code " + executor.getReturnCode());
                    if (executor.getReturnCode() == 0) {
                        Messaging.send(principal, Topics.INFORMATION, this,
                                       String.format("Docker image '%s' successfully registered", correctedName));
                        return;
                    } else {
                        logger.severe("Command output: " + sharedAccumulator.getOutput());
                    }
                } else {
                    Messaging.send(principal, Topics.INFORMATION, this,
                                   String.format("Docker image '%s' successfully registered", correctedName));
                    return;
                }
            } else {
                logger.severe("Command output: " + sharedAccumulator.getOutput());
            }
        }
        String message = String.format("Docker image '%s' failed to register. Details: '%s'",
                                       correctedName, sharedAccumulator.getOutput());
        sharedAccumulator.reset();
        logger.severe(message);
        Messaging.send(principal, Topics.ERROR, this, message);
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
        dockerListCmdTemplate.set(2, name);
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNodeInfo.getId(),
                                              masterNodeInfo.getUserName(),
                                              masterNodeInfo.getUserPass(),
                                              dockerListCmdTemplate, false, SSHMode.EXEC);
        sharedAccumulator.reset();
        final Executor executor = Executor.execute(sharedAccumulator, job);
        waitFor(executor, 50, TimeUnit.SECONDS);
        if (executor.getReturnCode() == 0) {
            String[] lines = sharedAccumulator.getOutput().split("\n");
            for (String line : lines) {
                String[] tokens = line.split(" |\t");
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
            String message = String.format("Docker command failed. Details: '%s'", sharedAccumulator.getOutput());
            sharedAccumulator.reset();
            logger.severe(message);
        }

        return container;
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
                                              dockerListAllCmd, false, SSHMode.EXEC);
        sharedAccumulator.reset();
        final Executor executor = Executor.execute(sharedAccumulator, job);
        waitFor(executor, 3, TimeUnit.SECONDS);
        if (executor.getReturnCode() == 0) {
            String[] lines = sharedAccumulator.getOutput().replace("\n", "").split(";");
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
                        getPersistenceManager().getContainerById(containerId);
                    }
                }
            }
        } else {
            String message = String.format("Docker command failed. Details: '%s'", sharedAccumulator.getOutput());
            sharedAccumulator.reset();
            logger.severe(message);
        }
        return containers;
    }

    private void onCompleted(NodeDescription node, ToolInstallStatus status) {
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
            masterNodeInfo = new NodeDescription();
            String hostName = InetAddress.getLocalHost().getHostName();
            masterNodeInfo.setId(hostName);
            Map<String, String> settings = ConfigurationManager.getInstance().getValues("topology.master");
            masterNodeInfo.setUserName(settings.get("topology.master.user"));
            masterNodeInfo.setUserPass(settings.get("topology.master.password"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new TopologyException("Master hostname retrieval failure", e);
        }
    }

    private PersistenceManager getPersistenceManager() {
        return this.persistenceManager.get();
    }
}
