/*
 * Copyright (C) 2017 CS ROMANIA
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

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.docker.Container;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Topics;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.spi.ServiceLoader;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.utils.Platform;
import ro.cs.tao.utils.async.BinaryTask;
import ro.cs.tao.utils.async.LazyInitialize;
import ro.cs.tao.utils.executors.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Cosmin Udroiu
 */
public class TopologyManager implements ITopologyManager {
    private static final TopologyManager instance;

    private final Logger logger;
    private NodeDescription masterNodeInfo;
    private final Set<TopologyToolInstaller> installers;
    private final Supplier<PersistenceManager> persistenceManager;
    private final ExecutorService executorService;

    static {
        instance = new TopologyManager();
    }

    private TopologyManager() {
        this.logger = Logger.getLogger(TopologyManager.class.getName());
        this.persistenceManager = LazyInitialize.using(() -> SpringContextBridge.services().getPersistenceManager());
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
                .filter(node -> node.getActive())     // we take only active node but not the deleted ones
                .collect(Collectors.toList());
    }

    @Override
    public void add(NodeDescription info) throws TopologyException {
        if (getPersistenceManager().checkIfExistsNodeByHostName(info.getHostName())) {
            try {
                getPersistenceManager().updateExecutionNode(info);
            } catch (PersistenceException e) {
                logger.severe("Cannot update node description to database. Rolling back installation on node " + info.getHostName() + "...");
                throw new TopologyException(e);
            }
        } else {
            try {
                getPersistenceManager().saveExecutionNode(info);
            } catch (PersistenceException e) {
                logger.severe("Cannot save node description to database. Rolling back installation on node " + info.getHostName() + "...");
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
            logger.severe("Cannot update node description in database for the host name:" + nodeInfo.getHostName());
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
    public List<Container> getAvailableDockerImages() {
        List<Container> containers = new ArrayList<>();
        List<String> args = new ArrayList<String>() {{
           add("docker");
           add("images");
           add("--format");
            //noinspection ConstantConditions
            add(Platform.ID.win != Platform.getCurrentPlatform().getId() ? "'table " : "'" + "{{.ID}}\\t{{.Tag}}\\t{{.Repository}}'");
        }};
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNodeInfo.getHostName(),
                                              masterNodeInfo.getUserName(),
                                              masterNodeInfo.getUserPass(),
                                              args, true, SSHMode.EXEC);
        List<String> lines = new ArrayList<>();
        final Executor executor = Executor.execute(new OutputConsumer() {
            @Override
            public void consume(String message) {
                lines.add(message);
            }
        }, job);
        try {
            executor.getWaitObject().await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warning("Process timed out: " + e.getMessage());
        }
        if (executor.getReturnCode() == 0) {
            for (int i = 0; i < lines.size(); i++) {
                String[] tokens = lines.get(i).split("\t");
                if (!"IMAGE_ID".equals(tokens[0])) {
                    Container container = new Container();
                    container.setId(tokens[0]);
                    container.setName(tokens[2].contains("/") ?
                                              tokens[2].substring(tokens[2].indexOf("/") + 1) :
                                              tokens[2]);
                    container.setTag(tokens[1]);
                    containers.add(container);
                    try {
                        final Container existing = getPersistenceManager().getContainerById(tokens[0]);
                        if (existing == null) {
                            getPersistenceManager().saveContainer(container);
                        }
                    } catch (PersistenceException e) {
                        logger.warning(e.getMessage());
                    }
                }
            }
        } else {
            logger.warning("Docker execution failed. Check that Docker is installed and the sudo credentials are valid");
            containers.addAll(getPersistenceManager().getContainers());
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
                                              node.getHostName()));
                break;
            case UNINSTALLED:
                Messaging.send(SystemPrincipal.instance(), Topics.INFORMATION,
                                       this,
                                       String.format("%s uninstallation on %s completed",
                                              status.getToolName(),
                                              node.getHostName()));
                break;
            case ERROR:
                Messaging.send(SystemPrincipal.instance(), Topics.WARNING,
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
