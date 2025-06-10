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

    /**
     * Sets the `masterNode` for the DockerManager instance.
     * The master node serves as the primary configuration or reference
     * point for managing Docker containers within the application.
     *
     * @param master the {@code NodeDescription} object representing the master node to be set
     */
    public static void setMasterNode(NodeDescription master) {
        instance.masterNode = master;
    }

    /**
     * Retrieves a list of available Docker container images.
     * This method internally delegates to an instance-specific implementation
     * to fetch the list of available Docker images.
     *
     * @return a list of {@code Container} objects representing available Docker container images,
     *         or an empty list if no images are available.
     */
    public static List<Container> getAvailableDockerImages() {
        return instance.listAvailableDockerImages();
    }

    /**
     * Lists Docker container images available on Docker Hub that match the specified filter.
     * The method queries Docker Hub using the provided filter criteria and retrieves a list of
     * container images that match the filter.
     *
     * @param filter a string representing the filter criteria to match the Docker images on Docker Hub
     * @return a list of {@code Container} objects representing Docker images that match the filter criteria,
     *         or an empty list if no images match the filter
     * @throws IOException if an error occurs during communication with Docker Hub
     */
    public static List<Container> listDockerHubImages(String filter) throws IOException {
        return instance.listFromDockerHub(filter);
    }

    /**
     * Retrieves a Docker container image by its name. If the image matches the provided name,
     * the corresponding Container object is returned. If the image is not found, the method
     * will return null.
     *
     * @param name the name of the Docker image to be retrieved
     * @return a {@code Container} object representing the Docker image, or null if no Docker image matches the given name
     */
    public static Container getDockerImage(String name) {
        return instance.get(name);
    }

    /**
     * Checks if a public Docker image with the specified name exists in the repository.
     *
     * @param name the name of the Docker image to check for existence
     * @return {@code true} if the Docker image exists, {@code false} otherwise
     */
    public static boolean publicImageExists(String name) {
        return instance.checkExists(name);
    }

    /**
     * Pulls a Docker image by its name. This method executes the necessary commands to fetch
     * the specified image from the Docker repository and creates a corresponding Container object.
     * If the image cannot be pulled or an error occurs, null is returned.
     *
     * @param name the name of the Docker image to pull, including the tag (e.g., "nginx:latest")
     * @return a {@code Container} object representing the pulled Docker image, or null if the pull operation fails
     */
    public static Container pullImage(String name) {
        return instance.pull(name);
    }

    /**
     * Pushes the specified container image to the Docker registry.
     *
     * @param image the container image that needs to be pushed to the registry
     * @return the identifier of the pushed image, or null if the operation fails
     */
    public static String pushImageToRegistry(Container image) {
        return instance.pushToRegistry(image);
    }

    /**
     * Runs a Docker container as a daemon using the specified image, name, port mapping, user, and additional arguments.
     * This method delegates to an internal implementation to execute the command and manage the container lifecycle.
     *
     * @param imageName the name of the Docker image to use for the container
     * @param name the name to assign to the running container, or null if no custom name is desired
     * @param portMap a {@code Tuple} specifying the host and container ports to map (host port as key, container port as value), or null if no ports need mapping
     * @param user the user associated with the container instance, typically for identification
     * @param arguments a list of additional command-line arguments to pass to the Docker run command
     * @return the unique identifier (ID) of the created container, or null if the operation fails
     * @throws IOException if an error occurs during command execution or communication with the Docker engine
     */
    public static String runDaemon(String imageName, String name, Tuple<Integer, Integer> portMap, String user, List<String> arguments) throws IOException {
        return instance.runAsDaemon(imageName, name, portMap, user, arguments);
    }

    /**
     * Runs a Docker container using the specified image and arguments, and waits for its completion
     * or until the specified timeout expires.
     *
     * @param containerName the name of the Docker container to run
     * @param arguments a list of additional command-line arguments to pass to the Docker run command
     * @param timeoutMinutes the maximum time in minutes to wait for the container's execution to complete
     * @return the output from the Docker command execution, or {@code null} if the container name or arguments are invalid
     * @throws IOException if an error occurs during execution or if the Docker command fails
     */
    public static String runWait(String containerName, List<String> arguments, long timeoutMinutes)  throws IOException {
        return instance.run(containerName, arguments, timeoutMinutes);
    }

    /**
     * Retrieves the IDs of Docker container instances currently running for the specified image name.
     * The method queries the running instances based on the provided image name and returns their unique identifiers.
     *
     * @param imageName the name of the Docker image for which the running instances are to be fetched
     * @return an array of strings containing the unique identifiers (IDs) of the running Docker container instances;
     *         may return an empty array if no instances are found
     */
    public static String[] getInstances(String imageName) {
        return instance.getRunningInstances(imageName);
    }

    /**
     * Retrieves the unique identifier (ID) of a running Docker container instance based on
     * the specified image name and container name. If a running instance matching the
     * provided parameters is found, its ID is returned.
     *
     * @param imageName the name of the Docker image for which the running instance is to be fetched
     * @param containerName the name of the Docker container to query
     * @return the unique identifier (ID) of the running Docker container instance,
     *         or null if no matching instance is found
     */
    public static String getInstance(String imageName, String containerName) {
        return instance.getRunningInstance(imageName, containerName);
    }

    /**
     * Retrieves the unique identifier (ID) of a running Docker container based on the provided container ID.
     * This method queries the underlying Docker engine to fetch the running instance associated with the given container ID.
     *
     * @param containerId the identifier of the container for which the running instance ID is to be retrieved
     * @return the unique identifier (ID) of the running Docker container instance, or null if no such instance is found
     */
    public static String getInstance(String containerId) {
        return instance.getRunningInstance(containerId);
    }

    /**
     * Registers a new Docker image by providing the path to the image, a short name, and a description.
     *
     * @param imagePath   the {@code Path} to the Docker image file to be registered
     * @param shortName   the short name to assign to the Docker image
     * @param description a description of the Docker image
     * @return the unique identifier (e.g., tag or name) of the registered Docker image
     * @throws TopologyException if the image registration process fails
     */
    public static String registerImage(Path imagePath, String shortName, String description) throws TopologyException {
        return instance.register(imagePath, shortName, description);
    }

    /**
     * Stops a running Docker container instance and removes it. This method identifies the
     * container instance based on the provided container ID and user ID, stops the container,
     * removes it from the Docker engine, and clears it from the internal instance provider.
     *
     * @param containerId the unique identifier of the Docker container instance to be stopped
     * @param userId the user ID associated with the container instance
     * @throws IOException if stopping or removing the container fails due to an execution error
     */
    public static void stopInstance(String containerId, String userId) throws IOException {
        instance.stop(containerId, userId);
    }

    /**
     * Stops all running instances of a Docker container with the specified name.
     * This method delegates to an internal implementation that halts all instances
     * associated with the provided container name.
     *
     * @param containerName the name of the Docker container whose instances are to be stopped
     * @throws IOException if an error occurs while stopping the instances
     */
    public static void stopInstances(String containerName) throws IOException {
        instance.stopAll(containerName);
    }

    /**
     * Retrieves the next available free port that can be used for allocating a new Docker container.
     * This method checks the currently allocated ports and calculates the next unused port
     * starting from a predefined base port.
     *
     * @return the next free port number as an integer
     */
    public static int getNextFreePort() {
        return instance.getAvailablePort();
    }

    private DockerManager() {
        this.logger = Logger.getLogger(TopologyManager.class.getName());
        this.containerProvider = SpringContextBridge.services().getService(ContainerProvider.class);
        this.containerInstanceProvider = SpringContextBridge.services().getService(ContainerInstanceProvider.class);
        this.basePort = Integer.parseInt(ConfigurationManager.getInstance().getValue("tao.docker.base.port", "8888"));
    }

    /**
     * Retrieves a list of available Docker images by comparing the locally detected
     * Docker containers with the database-stored Docker containers.
     *
     * The method fetches the list of Docker images from the system and filters them
     * to retain only those matching the Docker container entries stored in the database.
     * If no Docker images are detected locally, a warning is logged indicating potential
     * issues with the Docker setup or permissions.
     *
     * @return a list of Docker containers that are present both locally and in the database.
     */
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

    /**
     * Retrieves a list of containers from Docker Hub based on the specified filter.
     *
     * @param filter the search filter to apply when querying Docker Hub
     * @return a list of containers matching the filter criteria; if no containers are found,
     *         returns an empty list or null
     * @throws IOException if an I/O error occurs during the query process
     */
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

    /**
     * Retrieves a Container instance based on the specified name. This method executes a series of
     * commands to interact with Docker, parses the output, and constructs a Container object if a
     * valid result is found. In case of failure, logs an error with details of the issue.
     *
     * @param name the name of the container to be retrieved
     * @return a Container instance if successfully retrieved; null if the container is not found
     *         or if an error occurs
     */
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

    /**
     * Retrieves the list of currently running instance IDs for the specified Docker image.
     *
     * This method uses an external process to execute a command to fetch the
     * running instances related to the provided Docker image. It processes the
     * output to extract and return the instance IDs.
     *
     * @param imageName The name of the Docker image for which the running instance
     *                  IDs need to be retrieved.
     * @return An array of strings containing the IDs of the running instances
     *         associated with the specified image. Returns null if no running
     *         instances are found or if the command execution fails.
     */
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

    /**
     * Retrieves the running instance ID of a Docker container given its container ID.
     *
     * @param containerId the unique identifier of the Docker container whose running instance is to be retrieved
     * @return the running instance ID of the specified Docker container, or null if the instance could not be determined
     */
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

    /**
     * Retrieves the running instance ID of a Docker container based on the provided image name and container name.
     *
     * @param imageName the name of the Docker image
     * @param containerName the name of the Docker container
     * @return the 12-character ID of the running instance if found, or null otherwise
     */
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

    /**
     * Checks if a specific Docker image exists in the Docker Hub repository.
     *
     * @param name the name of the Docker image to be checked
     * @return {@code true} if the Docker image exists, {@code false} otherwise
     */
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

    /**
     * Pulls a Docker container with the given name, executing the necessary commands and
     * returning the associated Container object if the operation is successful.
     *
     * @param name the name of the Docker container to pull
     * @return the associated Container object if the operation is successful,
     *         or null if the operation fails
     */
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

    /**
     * Registers a Docker image located at the specified path with the provided details.
     * The method builds the Docker image, tags it, and optionally pushes it to a remote registry if configured.
     * This process involves multiple execution steps and may throw exceptions in case of errors.
     *
     * @param imagePath the path to the Docker image file. Must not be null and must exist.
     * @param shortName the short name for the image. Spaces will be replaced with hyphens.
     * @param description a description for the Docker image.
     * @return the unique identifier (container ID) of the registered Docker image if the process succeeds, or null otherwise.
     * @throws TopologyException if the image path is invalid or does not exist.
     */
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
            return pushToRegistry(image);
        }
        String message = String.format("Registration for image '%s' failed with code %s. Details: '%s'",
                                       correctedName, retCode, accumulator.getOutput());
        logger.severe(message);
        Messaging.send(principal, Topic.ERROR.value(), this, message);
        return containerId;
    }

    private String pushToRegistry(Container image) {
        if (image == null) {
            throw new IllegalArgumentException("[image] must not be null]");
        }
        String localRegistry = ConfigurationManager.getInstance().getValue("tao.docker.registry");
        Principal principal = SessionStore.currentContext() != null ?
                              SessionStore.currentContext().getPrincipal() : SystemPrincipal.instance();
        String name = image.getName();
        // do not execute docker tag is no docker registry is configured
        if (localRegistry == null || localRegistry.length() == 0) {
            Messaging.send(principal, Topic.INFORMATION.value(), this,
                           String.format("Docker image '%s' successfully registered", name));
            return image.getId();
        }
        String tag = localRegistry + "/" + name;
        List<String> commands = ProcessHelper.tokenizeCommands(dockerTagCmdTemplate, image.getId(), tag);
        ExecutionUnit job = new ExecutionUnit(ExecutorType.PROCESS,
                                              masterNode.getId(),
                                              masterNode.getUserName(),
                                              masterNode.getUserPass(),
                                              commands, false, SSHMode.EXEC);
        OutputAccumulator accumulator = new OutputAccumulator();
        accumulator.reset();
        Executor executor = Executor.execute(accumulator, job);
        logger.fine("Executing " + String.join(" ", commands));
        int retCode;
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
                                   String.format("Docker image '%s' successfully registered", name));
                    return image.getId();
                } else {
                    logger.severe("Command output: " + accumulator.getOutput());
                }
            } else {
                Messaging.send(principal, Topic.INFORMATION.value(), this,
                               String.format("Docker image '%s' successfully registered", name));
                return image.getId();
            }
        } else {
            logger.severe(String.format("Execution failed with code %s. Command output: %s",
                                        executor.getReturnCode(),
                                        accumulator.getOutput()));
        }
        String message = String.format("Registration for image '%s' failed with code %s. Details: '%s'",
                                       name, retCode, accumulator.getOutput());
        logger.severe(message);
        Messaging.send(principal, Topic.ERROR.value(), this, message);
        return null;
    }

    /**
     * Determines and returns the next available port starting from a base port.
     * It checks against a list of already allocated ports and ensures the returned port is not in use.
     *
     * @return the next available port number that is not currently allocated
     */
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

    /**
     * Executes a Docker container using the specified arguments and waits for the execution to finish
     * within the provided timeout period.
     *
     * @param containerName the name of the Docker container to run. Must not be null or empty.
     * @param arguments a list of command-line arguments to append to the Docker "run" command. Must not be null.
     * @param timeoutMinutes the maximum time in minutes to wait for the execution to complete.
     * @return the output of the executed command if the return code is non-zero.
     * @throws IOException if the Docker command fails or an error occurs during execution.
     */
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

    /**
     * Executes a Docker container as a detached daemon process based on the provided parameters.
     * The method constructs and executes a Docker run command based on the input details,
     * handles the output to retrieve the container ID, and saves the container metadata.
     *
     * @param imageName The name of the Docker image to run. Must not be null or empty.
     * @param name The name to assign to the container. Can be null if no specific name is desired.
     * @param portMap A Tuple containing the host port and container port mapping. Can be null if no ports need to be mapped.
     * @param user The user associated with the container execution. Can be null if no specific user is required.
     * @param arguments A list of arguments to pass to the Docker command. Must not be null.
     * @return The unique container ID of the created Docker container, or null if the creation fails.
     * @throws IOException If an I/O error occurs while executing the Docker process.
     */
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

    /**
     * Stops and removes a container instance based on the given container ID and user ID.
     * Executes Docker commands to stop and remove the container, and updates the container instance provider.
     * Throws an exception if the Docker commands fail.
     *
     * @param containerId the unique identifier of the container to be stopped
     * @param userId the unique identifier of the user associated with the container
     * @throws IOException if the Docker stop or remove commands fail to execute properly
     */
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

    /**
     * Stops and removes all Docker container instances associated with the specified container name.
     * This method retrieves the container instances based on the provided container name,
     * stops each container, removes them, and deletes their metadata from the container instance provider.
     * If any command fails during execution, an {@link IOException} is thrown.
     *
     * @param containerName the name of the container whose instances should be stopped and removed.
     * @throws IOException if a Docker command fails or encounters an issue during execution.
     */
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

    /**
     * Waits for a specific amount of time using the given executor's wait object.
     * Logs a warning if the wait times out or is interrupted.
     *
     * @param executor the executor providing the wait object for synchronization
     * @param amount the amount of time to wait
     * @param unit the time unit of the {@code amount} parameter
     */
    private void waitFor(Executor<?> executor, long amount, TimeUnit unit) {
        try {
            if (!executor.getWaitObject().await(amount, unit)) {
                logger.warning("Process timed out");
            }
        } catch (InterruptedException e) {
            logger.warning("Process was interrupted: " + e.getMessage());
        }
    }

    /**
     * Retrieves a list of Docker container images available on the system.
     * This method runs a Docker command to list all images, parses the command output,
     * and creates a list of {@link Container} objects with their associated details.
     *
     * The method will log an error if the Docker command fails to execute or produces no output.
     *
     * @return a {@code List} of {@link Container} objects representing the Docker images,
     *         including their ID, name, and tag; returns an empty list if no images are found or
     *         if the command execution fails.
     */
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
