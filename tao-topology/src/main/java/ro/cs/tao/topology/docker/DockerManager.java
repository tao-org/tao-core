package ro.cs.tao.topology.docker;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.configuration.Configuration;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.docker.Container;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Topic;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.TopologyException;
import ro.cs.tao.topology.TopologyManager;
import ro.cs.tao.utils.async.LazyInitialize;
import ro.cs.tao.utils.executors.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Singleton class to manage Docker container registration.
 *
 * @author Cosmin Cara
 */
public class DockerManager {

    private static final String dockerBuildCmdTemplate;
    private static final String dockerTagCmdTemplate;
    private static final String dockerPushCmdTemplate;
    private static final String dockerListCmdTemplate;
    private static final String dockerListAllCmd;
    private static final OutputAccumulator sharedAccumulator;
    private static final DockerManager instance;

    private final Supplier<PersistenceManager> persistenceManager;
    private final Logger logger;
    private NodeDescription masterNode;

    static {
        dockerBuildCmdTemplate = "docker build -t %s %s";
        dockerTagCmdTemplate = "docker tag %s %s";
        dockerPushCmdTemplate = "docker push %s";
        dockerListAllCmd = "docker images --format {{.ID}}\\t{{.Tag}}\\t{{.Repository}}";
        dockerListCmdTemplate = "docker images %s --format {{.ID}}\\t{{.Tag}}\\t{{.Repository}}";
        sharedAccumulator = new OutputAccumulator();
        instance = new DockerManager();
    }

    public static void setMasterNode(NodeDescription master) {
        instance.masterNode = master;
    }

    public static List<Container> getAvailableDockerImages() {
        return instance.listAvailableDockerImages();
    }

    public static Container getDockerImage(String name) {
        return instance.get(name);
    }

    public static String registerImage(Path imagePath, String shortName, String description) throws TopologyException {
        return instance.register(imagePath, shortName, description);
    }

    private DockerManager() {
        this.logger = Logger.getLogger(TopologyManager.class.getName());
        this.persistenceManager = LazyInitialize.using(() -> SpringContextBridge.services().getService(PersistenceManager.class));
    }

    private List<Container> listAvailableDockerImages() {
        List<Container> containers = getDockerImages();
        List<Container> dbContainers = getPersistenceManager().getContainers();
        if (containers.size() == 0) {
            logger.warning("Docker execution failed. Check that Docker is installed and the sudo credentials are valid");
        } else {
            dbContainers.retainAll(containers);
        }
        return dbContainers;
    }

    private Container get(String name) {
        Container container = null;
        final List<String> commands = ProcessHelper.tokenizeCommands(dockerListCmdTemplate, name);
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNode.getId(),
                                              masterNode.getUserName(),
                                              masterNode.getUserPass(),
                                              commands, false, SSHMode.EXEC);
        sharedAccumulator.reset();
        int retCode;
        final Executor<?> executor = Executor.execute(sharedAccumulator, job);
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

    private String register(Path imagePath, String shortName, String description) throws TopologyException {
        if (imagePath == null || !Files.exists(imagePath)) {
            throw new TopologyException("Invalid image path");
        }
        Principal principal = SessionStore.currentContext() != null ?
                SessionStore.currentContext().getPrincipal() : SystemPrincipal.instance();
        String correctedName = shortName.replace(" ", "-");
        List<String> commands = ProcessHelper.tokenizeCommands(dockerBuildCmdTemplate,
                                                               correctedName, imagePath.getParent().toString());
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNode.getId(),
                                              masterNode.getUserName(),
                                              masterNode.getUserPass(),
                                              commands, false, SSHMode.EXEC);
        sharedAccumulator.reset();
        int retCode;
        Executor<?> executor = Executor.execute(sharedAccumulator, job);
        logger.fine("Executing " + String.join(" ", commands));
        String containerId = null;
        waitFor(executor, 10, TimeUnit.MINUTES);
        if ((retCode = executor.getReturnCode()) == 0) {
            Container image = getDockerImage(correctedName);
            containerId = image.getId();
            String localRegistry = ConfigurationManager.getInstance().getValue(Configuration.Docker.REGISTRY_URL);
            String tag = localRegistry + "/" + correctedName;
            commands = ProcessHelper.tokenizeCommands(dockerTagCmdTemplate, containerId, tag);
            job = new ExecutionUnit(ExecutorType.PROCESS,
                                    masterNode.getId(),
                                    masterNode.getUserName(),
                                    masterNode.getUserPass(),
                                    commands, false, SSHMode.EXEC);
            sharedAccumulator.reset();
            executor = Executor.execute(sharedAccumulator, job);
            logger.fine("Executing " + String.join(" ", commands));
            waitFor(executor, 30, TimeUnit.SECONDS);
            if ((retCode = executor.getReturnCode()) == 0) {
                if (!SystemUtils.IS_OS_WINDOWS) {
                    commands = ProcessHelper.tokenizeCommands(dockerPushCmdTemplate, tag);
                    job = new ExecutionUnit(ExecutorType.PROCESS,
                                            masterNode.getId(),
                                            masterNode.getUserName(),
                                            masterNode.getUserPass(),
                                            commands, false, SSHMode.EXEC);
                    sharedAccumulator.reset();
                    executor = Executor.execute(sharedAccumulator, job);
                    logger.fine("Executing " + String.join(" ", commands));
                    waitFor(executor, 10, TimeUnit.MINUTES);
                    if ((retCode = executor.getReturnCode()) == 0) {
                        Messaging.send(principal, Topic.INFORMATION.value(), this,
                                       String.format("Docker image '%s' successfully registered", correctedName));
                        return containerId;
                    } else {
                        logger.severe("Command output: " + sharedAccumulator.getOutput());
                    }
                } else {
                    Messaging.send(principal, Topic.INFORMATION.value(), this,
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
        Messaging.send(principal, Topic.ERROR.value(), this, message);
        return containerId;
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

    private List<Container> getDockerImages() {
        List<Container> containers = new ArrayList<>();
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNode.getId(),
                                              masterNode.getUserName(),
                                              masterNode.getUserPass(),
                                              ProcessHelper.tokenizeCommands(dockerListAllCmd),
                                              false, SSHMode.EXEC);
        sharedAccumulator.reset();
        sharedAccumulator.preserveLineSeparator(true);
        final Executor<?> executor = Executor.execute(sharedAccumulator, job);
        waitFor(executor, 3, TimeUnit.SECONDS);
        if (executor.getReturnCode() == 0) {
            final String output = sharedAccumulator.getOutput();
            String[] lines = SystemUtils.IS_OS_WINDOWS ? output.split("\n") : output.replace("\n", "").split(";");
            for (String line : lines) {
                String[] tokens = line.split("[ \t]");
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
}
