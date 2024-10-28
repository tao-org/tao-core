package ro.cs.tao.topology.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ContainerInstance;
import ro.cs.tao.docker.ContainerType;
import ro.cs.tao.docker.ContainerVisibility;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Topic;
import ro.cs.tao.persistence.ContainerInstanceProvider;
import ro.cs.tao.persistence.ContainerProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.TopologyException;
import ro.cs.tao.topology.TopologyManager;
import ro.cs.tao.utils.NetUtils;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.utils.Tuple;
import ro.cs.tao.utils.executors.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private static final String dockerPullCmd;
    private static final String dockerRunningInstanceByPortCmd;
    private static final String dockerRunningInstanceCmd;
    private static final String dockerRunningInstanceByIdCmd;
    private static final String dockerRunningInstancesCmd;
    private static final String dockerHubCheckImageCmd;
    private static final DockerManager instance;

    private final ContainerProvider containerProvider;
    private final ContainerInstanceProvider containerInstanceProvider;
    private final Logger logger;
    private final int basePort;
    private NodeDescription masterNode;

    static {
        dockerBuildCmdTemplate = "docker build -t %s %s";
        dockerTagCmdTemplate = "docker tag %s %s";
        dockerPushCmdTemplate = "docker push %s";
        dockerListAllCmd = "docker images --format {{.ID}}\\t{{.Tag}}\\t{{.Repository}}";
        dockerListCmdTemplate = "docker images %s --format {{.ID}}\\t{{.Tag}}\\t{{.Repository}}";
        dockerPullCmd = "docker pull %s";
        dockerRunningInstanceByPortCmd = "docker ps --filter \"expose=%s\"";
        dockerRunningInstanceCmd = "docker ps --filter \"ancestor=%s\" --filter \"name=%s\" --filter \"status=running\"";
        dockerRunningInstanceByIdCmd = "docker ps --filter \"id=%s\" --filter \"status=running\"";
        dockerRunningInstancesCmd = "docker ps --filter \"ancestor=%s\" --filter \"status=running\"";
        dockerHubCheckImageCmd = SystemUtils.IS_OS_WINDOWS
                                 ? "docker manifest inspect %s > NUL & echo %%ERRORLEVEL%%"
                                 : "docker manifest inspect %s > /dev/null ; echo $?";
        instance = new DockerManager();
    }

    public static void setMasterNode(NodeDescription master) {
        instance.masterNode = master;
    }

    public static List<Container> getAvailableDockerImages() {
        return instance.listAvailableDockerImages();
    }

    public static List<Container> listDockerHubImages(String filter) throws IOException {
        return instance.listFromDockerHub(filter);
    }

    public static Container getDockerImage(String name) {
        return instance.get(name);
    }

    public static boolean publicImageExists(String name) {
        return instance.checkExists(name);
    }

    public static Container pullImage(String name) {
        return instance.pull(name);
    }

    public static String runDaemon(String imageName, String name, Tuple<Integer, Integer> portMap, String user, List<String> arguments) throws IOException {
        return instance.runAsDaemon(imageName, name, portMap, user, arguments);
    }

    public static String runWait(String containerName, List<String> arguments, long timeoutMinutes)  throws IOException {
        return instance.run(containerName, arguments, timeoutMinutes);
    }

    public static String[] getInstances(String imageName) {
        return instance.getRunningInstances(imageName);
    }

    public static String getInstance(String imageName, String containerName) {
        return instance.getRunningInstance(imageName, containerName);
    }

    public static String getInstance(String containerId) {
        return instance.getRunningInstance(containerId);
    }

    public static String registerImage(Path imagePath, String shortName, String description) throws TopologyException {
        return instance.register(imagePath, shortName, description);
    }

    public static void stopInstance(String containerId, String userId) throws IOException {
        instance.stop(containerId, userId);
    }

    public static void stopInstances(String containerName) throws IOException {
        instance.stopAll(containerName);
    }

    public static int getNextFreePort() {
        return instance.getAvailablePort();
    }

    private DockerManager() {
        this.logger = Logger.getLogger(TopologyManager.class.getName());
        this.containerProvider = SpringContextBridge.services().getService(ContainerProvider.class);
        this.containerInstanceProvider = SpringContextBridge.services().getService(ContainerInstanceProvider.class);
        this.basePort = Integer.parseInt(ConfigurationManager.getInstance().getValue("tao.docker.base.port", "8888"));
    }

    private List<Container> listAvailableDockerImages() {
        List<Container> containers = getDockerImages();
        List<Container> dbContainers = containerProvider.getByType(ContainerType.DOCKER);
        if (containers.isEmpty()) {
            logger.warning("Docker execution failed. Check that Docker is installed and the sudo credentials are valid");
        } else {
            dbContainers.retainAll(containers);
        }
        return dbContainers;
    }

    private List<Container> listFromDockerHub(String filter) throws IOException {
        List<Container> results = null;
        final String strUrl = ConfigurationManager.getInstance().getValue("docker.hub.search.url",
                                                                          "https://hub.docker.com/api/content/v1/products/search");
        final List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("q", filter));
        params.add(new BasicNameValuePair("source", "community"));
        params.add(new BasicNameValuePair("page", "1"));
        params.add(new BasicNameValuePair("page_size", "100"));
        final String result = NetUtils.getResponseAsString(strUrl, params);
        final ObjectReader reader = new ObjectMapper().readerFor(DockerHubResponse.class);
        DockerHubResponse response = reader.readValue(result);
        if (response != null && response.getCount() > 0) {
            results = new ArrayList<>();
            final List<DockerHubRecord> summaries = response.getSummaries();
            Container container;
            for (DockerHubRecord record : summaries) {
                container = new Container();
                container.setId(StringUtilities.isNullOrEmpty(record.getId())
                                ? record.getName()
                                : record.getId());
                container.setName(record.getName());
                container.setDescription(record.getShortDescription());
                if (!StringUtilities.isNullOrEmpty(record.getSource())) {
                    container.setTag(record.getSource());
                }
                container.setType(ContainerType.DOCKER);
                container.setVisibility(ContainerVisibility.PRIVATE);
                results.add(container);
            }
        }
        return results;
    }

    private Container get(String name) {
        Container container = null;
        final List<String> commands = ProcessHelper.tokenizeCommands(dockerListCmdTemplate, name);
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNode.getId(),
                                              masterNode.getUserName(),
                                              masterNode.getUserPass(),
                                              commands, false, SSHMode.EXEC);
        final OutputAccumulator accumulator = new OutputAccumulator();
        accumulator.preserveLineSeparator(true);
        int retCode;
        final Executor<?> executor = Executor.execute(accumulator, job);
        waitFor(executor, 50, TimeUnit.SECONDS);
        if ((retCode = executor.getReturnCode()) == 0) {
            String[] lines = accumulator.getOutput().split("\n");
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
                                           retCode, accumulator.getOutput());
            logger.severe(message);
        }

        return container;
    }

    private String[] getRunningInstances(String imageName) {
        final List<String> commands = ProcessHelper.tokenizeCommands(dockerRunningInstancesCmd, imageName);
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNode.getId(),
                                              masterNode.getUserName(),
                                              masterNode.getUserPass(),
                                              commands, false, SSHMode.EXEC);
        final OutputAccumulator accumulator = new OutputAccumulator();
        accumulator.preserveLineSeparator(true);
        final Executor<?> executor = Executor.execute(accumulator, job);
        String[] ids = null;
        if (executor.getReturnCode() == 0) {
            String[] lines = accumulator.getOutput().split("\n");
            if (lines.length >= 2) {
                ids = new String[lines.length - 1];
                for (int i = 0; i < ids.length; i++) {
                    ids[i] = lines[i + 1].substring(0, 12);
                }
            }
        }
        return ids;
    }

    private String getRunningInstance(String containerId) {
        final List<String> commands = ProcessHelper.tokenizeCommands(dockerRunningInstanceByIdCmd, containerId);
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNode.getId(),
                                              masterNode.getUserName(),
                                              masterNode.getUserPass(),
                                              commands, false, SSHMode.EXEC);
        final OutputAccumulator accumulator = new OutputAccumulator();
        accumulator.preserveLineSeparator(true);
        final Executor<?> executor = Executor.execute(accumulator, job);
        String id = null;
        if (executor.getReturnCode() == 0) {
            String[] lines = accumulator.getOutput().split("\n");
            if (lines.length == 2) {
                id = lines[1].substring(0, 12);
            }
        }
        return id;
    }

    private String getRunningInstance(String imageName, String containerName) {
        final List<String> commands = ProcessHelper.tokenizeCommands(dockerRunningInstanceCmd, imageName, containerName);
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNode.getId(),
                                              masterNode.getUserName(),
                                              masterNode.getUserPass(),
                                              commands, false, SSHMode.EXEC);
        final OutputAccumulator accumulator = new OutputAccumulator();
        accumulator.preserveLineSeparator(true);
        final Executor<?> executor = Executor.execute(accumulator, job);
        String id = null;
        if (executor.getReturnCode() == 0) {
            String[] lines = accumulator.getOutput().split("\n");
            if (lines.length == 2) {
                id = lines[1].substring(0, 12);
            }
        }
        return id;
    }

    private boolean checkExists(String name) {
        final List<String> commands = ProcessHelper.tokenizeCommands(dockerHubCheckImageCmd, name);
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNode.getId(),
                                              masterNode.getUserName(),
                                              masterNode.getUserPass(),
                                              commands, false, SSHMode.EXEC);
        final OutputAccumulator accumulator = new OutputAccumulator();
        accumulator.preserveLineSeparator(false);
        int retCode;
        final Executor<?> executor = Executor.execute(accumulator, job);
        waitFor(executor, 10, TimeUnit.SECONDS);
        if ((retCode = executor.getReturnCode()) == 0) {
            final String output = accumulator.getOutput();
            return "0".equals(output.substring(output.length() - 1));
        } else {
            String message = String.format("Docker command failed wit code %s. Details: '%s'",
                                           retCode, accumulator.getOutput());
            logger.severe(message);
            return false;
        }
    }

    private Container pull(String name) {
        Container container = null;
        final List<String> commands = ProcessHelper.tokenizeCommands(dockerPullCmd, name);
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNode.getId(),
                                              masterNode.getUserName(),
                                              masterNode.getUserPass(),
                                              commands, false, SSHMode.EXEC);
        final OutputAccumulator accumulator = new OutputAccumulator();
        accumulator.preserveLineSeparator(true);
        int retCode;
        final Executor<?> executor = Executor.execute(accumulator, job);
        waitFor(executor, 300, TimeUnit.SECONDS);
        if ((retCode = executor.getReturnCode()) == 0) {
            container = get(name);
        } else {
            String message = String.format("Docker command failed wit code %s. Details: '%s'",
                                           retCode, accumulator.getOutput());
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
        final OutputAccumulator accumulator = new OutputAccumulator();
        accumulator.preserveLineSeparator(true);
        int retCode;
        Executor<?> executor = Executor.execute(accumulator, job);
        logger.fine("Executing " + String.join(" ", commands));
        String containerId = null;
        waitFor(executor, 20, TimeUnit.MINUTES);
        if ((retCode = executor.getReturnCode()) == 0) {
            Container image = getDockerImage(correctedName);
            containerId = image.getId();
            String localRegistry = ConfigurationManager.getInstance().getValue("tao.docker.registry");
            // do not execute docker tag is no docker registry is configured
            if (localRegistry == null || localRegistry.length() == 0) {
                Messaging.send(principal, Topic.INFORMATION.value(), this,
                        String.format("Docker image '%s' successfully registered", correctedName));
                return containerId;
            }
            String tag = localRegistry + "/" + correctedName;
            commands = ProcessHelper.tokenizeCommands(dockerTagCmdTemplate, containerId, tag);
            job = new ExecutionUnit(ExecutorType.PROCESS,
                                    masterNode.getId(),
                                    masterNode.getUserName(),
                                    masterNode.getUserPass(),
                                    commands, false, SSHMode.EXEC);
            accumulator.reset();
            executor = Executor.execute(accumulator, job);
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
                    accumulator.reset();
                    executor = Executor.execute(accumulator, job);
                    logger.fine("Executing " + String.join(" ", commands));
                    waitFor(executor, 10, TimeUnit.MINUTES);
                    if ((retCode = executor.getReturnCode()) == 0) {
                        Messaging.send(principal, Topic.INFORMATION.value(), this,
                                       String.format("Docker image '%s' successfully registered", correctedName));
                        return containerId;
                    } else {
                        logger.severe("Command output: " + accumulator.getOutput());
                    }
                } else {
                    Messaging.send(principal, Topic.INFORMATION.value(), this,
                                   String.format("Docker image '%s' successfully registered", correctedName));
                    return containerId;
                }
            } else {
                logger.severe(String.format("Execution failed with code %s. Command output: %s",
                                            executor.getReturnCode(),
                                            accumulator.getOutput()));
            }
        }
        String message = String.format("Registration for image '%s' failed with code %s. Details: '%s'",
                                       correctedName, retCode, accumulator.getOutput());
        logger.severe(message);
        Messaging.send(principal, Topic.ERROR.value(), this, message);
        return containerId;
    }

    private int getAvailablePort() {
        final List<Integer> ports = containerInstanceProvider.getAllocatedPorts();
        if (ports == null || ports.isEmpty()) {
            return basePort;
        }
        final Set<Integer> distinctPorts = new HashSet<>(ports);
        int currentPort = basePort;
        while (distinctPorts.contains(currentPort)) {
            currentPort++;
        }
        return currentPort;
    }

    private String run(String containerName, List<String> arguments, long timeoutMinutes)  throws IOException {
        if (StringUtilities.isNullOrEmpty(containerName) || arguments == null) {
            return null;
        }
        arguments.add(0, "docker");
        arguments.add(1, "run");
        arguments.add(containerName);
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNode.getId(),
                                              masterNode.getUserName(),
                                              masterNode.getUserPass(),
                                              arguments, false, SSHMode.EXEC);
        final OutputAccumulator accumulator = new OutputAccumulator();
        accumulator.preserveLineSeparator(true);
        int retCode;
        final Executor<?> executor = Executor.execute(accumulator, job);
        waitFor(executor, timeoutMinutes, TimeUnit.MINUTES);
        if ((retCode = executor.getReturnCode()) != 0) {
            return accumulator.getOutput();
        } else {
            String message = String.format("Docker command failed wit code %s. Details: '%s'",
                                           retCode, accumulator.getOutput());
            throw new IOException(message);
        }
    }

    private String runAsDaemon(String imageName, String name, Tuple<Integer, Integer> portMap, String user, List<String> arguments) throws IOException {
        if (StringUtilities.isNullOrEmpty(imageName) || arguments == null) {
            return null;
        }
        int idx = 0;
        arguments.add(idx++, "docker");
        arguments.add(idx++, "run");
        arguments.add(idx++, "-d");
        if (!StringUtilities.isNullOrEmpty(name)) {
            arguments.add(idx++, "--name");
            arguments.add(idx++, name);
        }
        if (portMap != null) {
            arguments.add(idx++, "-p");
            arguments.add(idx, portMap.getKeyOne() + ":" + portMap.getKeyTwo());
        }
        arguments.add(imageName);
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNode.getId(),
                                              masterNode.getUserName(),
                                              masterNode.getUserPass(),
                                              arguments, false, SSHMode.EXEC);
        final OutputAccumulator accumulator = new OutputAccumulator();
        accumulator.preserveLineSeparator(true);
        final Executor<?> executor = Executor.execute(accumulator, job);
        String id = null;
        try {
            executor.getWaitObject().await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warning(e.getMessage());
        }
        if (executor.getReturnCode() == 0) {
            String[] lines = accumulator.getOutput().split("\n");
            if (lines.length == 1) {
                id = lines[0].substring(0, 12);
                final ContainerInstance entity = new ContainerInstance();
                entity.setId(id);
                entity.setContainerId(containerProvider.getByName(imageName).getId());
                entity.setName(name);
                if (portMap != null) {
                    entity.setPort(portMap.getKeyOne());
                }
                entity.setUserId(user);
                try {
                    containerInstanceProvider.save(entity);
                } catch (PersistenceException e) {
                    logger.severe(e.getMessage());
                }
            }
        }
        return id;
    }

    private void stop(String containerId, String userId) throws IOException {
        final ContainerInstance instance = containerInstanceProvider.getByContainerIdAndUserId(containerId, userId);
        if (instance == null) {
            return; // no instance
        }
        List<String> args = new ArrayList<>() {{
            add("docker");
            add("stop");
            add("-t");
            add("10");
            add(instance.getId());
        }};
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNode.getId(),
                                              masterNode.getUserName(),
                                              masterNode.getUserPass(),
                                              args, false, SSHMode.EXEC);
        final OutputAccumulator accumulator = new OutputAccumulator();
        accumulator.preserveLineSeparator(true);
        int retCode;
        Executor<?> executor = Executor.execute(accumulator, job);
        waitFor(executor, 15, TimeUnit.SECONDS);
        if ((retCode = executor.getReturnCode()) != 0) {
            String message = String.format("Docker command failed wit code %s. Details: '%s'",
                                           retCode, accumulator.getOutput());
            throw new IOException(message);
        } else {
            accumulator.reset();
            args.clear();
            args.add("docker");
            args.add("rm");
            args.add(instance.getId());
            job = new ExecutionUnit(ExecutorType.PROCESS,
                                    masterNode.getId(),
                                    masterNode.getUserName(),
                                    masterNode.getUserPass(),
                                    args, false, SSHMode.EXEC);
            executor = Executor.execute(accumulator, job);
            waitFor(executor, 15, TimeUnit.SECONDS);
            if ((retCode = executor.getReturnCode()) != 0) {
                String message = String.format("Docker command failed wit code %s. Details: '%s'",
                                               retCode, accumulator.getOutput());
                throw new IOException(message);
            }
        }
        try {
            containerInstanceProvider.delete(instance.getId());
        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
        }
    }

    private void stopAll(String containerName) throws IOException {
        final String[] ids = getInstances(containerName);
        if (ids != null) {
            for (String id : ids) {
                List<String> args = new ArrayList<>() {{
                    add("docker");
                    add("stop");
                    add("-t");
                    add("10");
                    add(id);
                }};
                ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                                      masterNode.getId(),
                                                      masterNode.getUserName(),
                                                      masterNode.getUserPass(),
                                                      args, false, SSHMode.EXEC);
                final OutputAccumulator accumulator = new OutputAccumulator();
                accumulator.preserveLineSeparator(true);
                int retCode;
                Executor<?> executor = Executor.execute(accumulator, job);
                waitFor(executor, 15, TimeUnit.SECONDS);
                if ((retCode = executor.getReturnCode()) != 0) {
                    String message = String.format("Docker command failed wit code %s. Details: '%s'",
                                                   retCode, accumulator.getOutput());
                    throw new IOException(message);
                } else {
                    accumulator.reset();
                    args.clear();
                    args.add("docker");
                    args.add("rm");
                    args.add(id);
                    job = new ExecutionUnit(ExecutorType.PROCESS,
                                            masterNode.getId(),
                                            masterNode.getUserName(),
                                            masterNode.getUserPass(),
                                            args, false, SSHMode.EXEC);
                    executor = Executor.execute(accumulator, job);
                    waitFor(executor, 15, TimeUnit.SECONDS);
                    if ((retCode = executor.getReturnCode()) != 0) {
                        String message = String.format("Docker command failed wit code %s. Details: '%s'",
                                                       retCode, accumulator.getOutput());
                        throw new IOException(message);
                    } else {
                        try {
                            containerInstanceProvider.delete(id);
                        } catch (PersistenceException e) {
                            logger.severe(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void waitFor(Executor<?> executor, long amount, TimeUnit unit) {
        try {
            if (!executor.getWaitObject().await(amount, unit)) {
                logger.warning("Process timed out");
            }
        } catch (InterruptedException e) {
            logger.warning("Process was interrupted: " + e.getMessage());
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
        final OutputAccumulator accumulator = new OutputAccumulator();
        accumulator.preserveLineSeparator(true);
        final Executor<?> executor = Executor.execute(accumulator, job);
        waitFor(executor, 3, TimeUnit.SECONDS);
        if (executor.getReturnCode() == 0) {
            final String output = accumulator.getOutput();
            //String[] lines = SystemUtils.IS_OS_WINDOWS ? output.split("\n") : output.replace("\n", "").split(";");
            String[] lines = output.split("[\n;]");
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
            String message = String.format("Docker command failed. Details: '%s'", accumulator.getOutput());
            logger.severe(message);
        }
        return containers;
    }
}
