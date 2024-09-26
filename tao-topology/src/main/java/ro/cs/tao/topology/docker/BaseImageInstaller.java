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

package ro.cs.tao.topology.docker;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.Tag;
import ro.cs.tao.component.*;
import ro.cs.tao.component.enums.ParameterType;
import ro.cs.tao.component.enums.ProcessingComponentType;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.enums.TagType;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ContainerType;
import ro.cs.tao.docker.ContainerVisibility;
import ro.cs.tao.persistence.ContainerProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.ProcessingComponentProvider;
import ro.cs.tao.persistence.TagProvider;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.utils.JacksonUtil;
import ro.cs.tao.utils.StringUtilities;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class BaseImageInstaller implements DockerImageInstaller {
    //protected static final Set<String> winExtensions = new HashSet<String>() {{ add(".bat"); add(".exe"); }};
    protected static final Path dockerPath;
    protected static final Path dockerImagesPath;
    protected final Logger logger;
    protected final TagProvider tagProvider;
    protected final ContainerProvider containerProvider;
    protected final ProcessingComponentProvider componentProvider;
    protected final Properties additionalProperties;
    private List<Tag> componentTags;

    static {
        final Logger log = Logger.getLogger(BaseImageInstaller.class.getName());
        dockerPath = findInPath("docker" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
        if (dockerPath == null) {
            log.warning("[docker] was not found in system path");
        }
        final String cfgValue = ConfigurationManager.getInstance().getValue("tao.docker.images");
        if (cfgValue == null) {
            log.warning("Invalid path for docker images");
            throw new RuntimeException("Invalid path for docker images");
        } else {
            dockerImagesPath = Paths.get(cfgValue);
            try {
                FileUtilities.createDirectories(dockerImagesPath);
            } catch (IOException e) {
                log.severe(e.getMessage());
            }
        }
    }

    public BaseImageInstaller() {
        this.tagProvider = SpringContextBridge.services().getService(TagProvider.class);
        this.containerProvider = SpringContextBridge.services().getService(ContainerProvider.class);
        this.componentProvider = SpringContextBridge.services().getService(ProcessingComponentProvider.class);
        this.logger = Logger.getLogger(getClass().getName());
        this.componentTags = this.tagProvider.list();
        this.additionalProperties = new Properties();
        if (this.componentTags == null) {
            this.componentTags = new ArrayList<>();
        }
    }

    @Override
    public Container installImage() {
        Container container = null;
        if (dockerPath != null) {
            try {
                final String localRegistry = ConfigurationManager.getInstance().getValue("tao.docker.registry");
                if ((container = DockerManager.getDockerImage(localRegistry + "/" + getContainerName())) == null &&
                    (container = DockerManager.getDockerImage(getContainerName())) == null) {
                    // first try the container.properties file, maybe it is a container from Docker Hub
                    Path dockerfilePath = dockerImagesPath.resolve(getContainerName()).resolve("container.properties");
                    Files.createDirectories(dockerImagesPath.resolve(getContainerName()));
                    //dockerfilePath = dockerfilePath.resolve("container.properties");
                    final byte[] buffer = new byte[1024];
                    try (InputStream is = getClass().getResourceAsStream("container.properties");
                         OutputStream os = new BufferedOutputStream(Files.newOutputStream(dockerfilePath))) {
                        if (is != null) {
                            int read;
                            while ((read = is.read(buffer)) != -1) {
                                os.write(buffer, 0, read);
                            }
                            os.flush();
                        }
                    } catch (Exception notFound) {
                        logger.fine(String.format("Container %s doesn't have container.properties", getContainerName()));
                    }
                    if (Files.exists(dockerfilePath)) {
                        additionalProperties.load(Files.newBufferedReader(dockerfilePath));
                    }
                    if (!additionalProperties.isEmpty()) {
                        final String dockerHubName = additionalProperties.getProperty("docker.hub.name");
                        if (!StringUtilities.isNullOrEmpty(dockerHubName)) {
                            container = DockerManager.pullImage(dockerHubName);
                        }
                    } else {
                        dockerfilePath = dockerImagesPath.resolve(getContainerName()).resolve("Dockerfile");
                        if (!Files.exists(dockerfilePath) || Files.size(dockerfilePath) == 0) {
                            Files.deleteIfExists(dockerfilePath);
                            logger.finest(String.format("Extracting Dockerfile for image %s", getContainerName()));
                            Files.write(dockerfilePath, readResource("Dockerfile"));
                        }
                        container = DockerManager.getDockerImage(getContainerName());
                    }
                    if (container == null) {
                        this.logger.info(String.format("Image %s was not found in Docker registry. Registration starting.\n" +
                                "Until registration completes, the corresponding components will not be available.", getContainerName()));
                        for (String resource : additionalResources()) {
                            //Files.write(dockerfilePath.getParent().resolve(Paths.get(resource).getFileName()), readResource(resource));
                            Files.write(dockerfilePath.getParent().resolve(Paths.get(resource)),
                                        readResource(resource));
                        }
                        DockerManager.registerImage(dockerfilePath.toRealPath(), getContainerName(), getDescription());
                        this.logger.info(String.format("Registration completed for docker image %s.", getContainerName()));
                        container = DockerManager.getDockerImage(getContainerName());
                    } else {
                        logger.finest(String.format("Image %s was found in Docker registry", getContainerName()));
                    }
                }
            } catch (IOException e) {
                logger.warning(String.format("Error occurred while registering %s: %s",
                                             getContainerName(), e.getMessage()));
            }
        }
        Container dbContainer = this.containerProvider.get(container != null ? container.getId() : getContainerName());
        if (dbContainer == null) {
            logger.info(String.format("Container %s not registered in database, will create one", getContainerName()));
            dbContainer = new Container();
            dbContainer.setId(container != null ? container.getId() : getContainerName());
            dbContainer.setName(getContainerName());
            dbContainer.setTag(container != null ? container.getTag() : getContainerName());
            dbContainer.setType(ContainerType.DOCKER);
            dbContainer.setVisibility(ContainerVisibility.PUBLIC);
            dbContainer = initializeContainer(dbContainer, dockerPath != null ? getPathInContainer() : getPathInSystem());
            if (dbContainer == null) {
                logger.severe(String.format("Container %s failed to register", getContainerName()));
            } else {
                logger.fine(String.format("Container %s registered with id '%s'", getContainerName(), dbContainer.getId()));
            }
        }
        return dbContainer;
    }

    protected Properties getAdditionalProperties() {
        Path dockerfilePath = dockerImagesPath.resolve(getContainerName()).resolve("container.properties");
        if (Files.exists(dockerfilePath)) {
            try (InputStream stream = Files.newInputStream(dockerfilePath)) {
                additionalProperties.load(stream);
            } catch (IOException e) {
                logger.warning("Cannot read file " + dockerfilePath);
            }
        }
        return additionalProperties;
    }

    protected Container readContainerDescriptor(String fileName) throws IOException {
        Container container;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName)))) {
            final String str = reader.lines().collect(Collectors.joining(""));
            container = JacksonUtil.fromString(str, Container.class);
        }
        container.setType(ContainerType.DOCKER);
        return container;
    }

    protected ProcessingComponent[] readComponentDescriptors(String fileName) throws IOException {
        ProcessingComponent[] components;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName)))) {
            final String str = reader.lines().collect(Collectors.joining(""));
            final ObjectMapper mapper = new ObjectMapper();
            final ObjectMapper innerMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(ParameterDescriptor.class, new JsonDeserializer<>() {
                @Override
                public ParameterDescriptor deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
                    JsonNode node = p.readValueAsTree();
                    try (JsonParser parser = innerMapper.createParser(node.toString())) {
                        final ObjectCodec codec = parser.getCodec();
                        JsonNode innerNode = parser.readValueAsTree();
                        if (ParameterType.TEMPLATE.name().equals(innerNode.get("type").textValue())) {
                            return codec.treeToValue(innerNode, TemplateParameterDescriptor.class);
                        } else {
                            return codec.treeToValue(innerNode, ParameterDescriptor.class);
                        }
                    }
                }
            });
            mapper.registerModule(module);
            components = mapper.readValue(str, ProcessingComponent[].class);
        }
        return components;
    }

    protected String readContainerLogo(String fileName) throws IOException {
        if (!StringUtilities.isNullOrEmpty(fileName)) {
            try (InputStream in = getClass().getResourceAsStream(fileName)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                if (in == null) return null;
                int read;
                byte[] buffer = new byte[1024];
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
                return Base64.getEncoder().encodeToString(out.toByteArray());
            }
        }
        return null;
    }

    protected byte[] readResource(String name) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(name)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (in == null) return null;
            int read;
            byte[] buffer = new byte[1024];
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            return out.toByteArray();
        }
    }

    protected String[] additionalResources() { return new String[0]; }

    protected static Path findInPath(String executableName) {
        String systemPath = System.getenv("Path");
        if (systemPath == null) {
            systemPath = System.getenv("PATH");
        }
        if (systemPath == null) {
            throw new RuntimeException("Cannot read system PATH");
        }
        String[] paths = systemPath.split(File.pathSeparator);
        Path currentPath = null;
        for (String path : paths) {
            currentPath = Paths.get(path).resolve(executableName);
            if (Files.exists(currentPath)) {
                break;
            } else {
                currentPath = null;
            }
        }
        return currentPath;
    }

    protected Tag getOrCreateTag(String tagText) throws PersistenceException {
        Tag tag = this.componentTags.stream().filter(t -> t.getText().equalsIgnoreCase(tagText)).findFirst().orElse(null);
        if (tag == null) {
            tag = this.tagProvider.save(new Tag(TagType.COMPONENT, tagText));
            this.componentTags.add(tag);
        }
        return tag;
    }

    protected Container initializeContainer(Container container, String path) {
        Container readContainer;
        try {
            readContainer = readContainerDescriptor(getContainerDescriptorFileName());
            readContainer.setId(container.getId());
            readContainer.setName(container.getName());
            readContainer.setTag(container.getTag());
            readContainer.setApplicationPath(path);
            readContainer.getApplications().forEach(this::configureApplication);
            readContainer.setLogo(readContainerLogo(getLogoFileName()));
            readContainer.setOwnerId(SystemPrincipal.instance().getName());
            readContainer.setVisibility(container.getVisibility());
            readContainer = this.containerProvider.save(readContainer);
        } catch (Exception e) {
            logger.severe(e.getMessage());
            return null;
        }
        ProcessingComponent current = null;
        try {
            ProcessingComponent[] components = readComponentDescriptors(getComponentDescriptorFileName());
            final List<Application> containerApplications = readContainer.getApplications();
            for (ProcessingComponent component : components) {
                try {
                    current = component;
                    component.setContainerId(readContainer.getId());
                    component.setLabel(component.getId());
                    component.setComponentType(ProcessingComponentType.EXECUTABLE);
                    containerApplications.stream().filter(a -> a.getName().equals(component.getId()) ||
                                                               a.getName().equals(component.getLabel())).findFirst()
                                         .ifPresent(application -> component.setFileLocation(application.getPath()));
                    Set<ParameterDescriptor> parameterDescriptors = component.getParameterDescriptors();
                    if (parameterDescriptors != null) {
                        parameterDescriptors.forEach(p -> {
                            if (p.getName() == null) {
                                p.setName(p.getId());
                                p.setId(UUID.randomUUID().toString());
                            }
                            String[] valueSet = p.getValueSet();
                            if (valueSet != null && valueSet.length == 1 &&
                                    ("null".equals(valueSet[0]) || valueSet[0].isEmpty())) {
                                p.setValueSet(null);
                            }
                            if (valueSet != null && valueSet.length > 0 &&
                                    ("null".equals(valueSet[0]) || valueSet[0].isEmpty())) {
                                p.setDefaultValue(valueSet[0]);
                            }
                        });
                    }
                    final List<SourceDescriptor> sources = component.getSources();
                    if (sources != null) {
                        sources.forEach(s -> {
                            s.setId(UUID.randomUUID().toString());
                            s.setParentId(component.getId());
                        });
                    }
                    final List<TargetDescriptor> targets = component.getTargets();
                    if (targets != null) {
                        targets.forEach(t -> {
                            t.setId(UUID.randomUUID().toString());
                            t.setParentId(component.getId());
                        });
                    }
                    String template = component.getTemplateContents();
                    final int length = template.length();
                    int i = 0;
                    while (i < length) {
                        char ch = template.charAt(i);
                        if (ch == '$' && template.charAt(i - 1) != '\n' && template.charAt(i - 1) != '=') {
                            template = template.substring(0, i) + "\n" + template.substring(i);
                        }
                        i++;
                    }
                    component.setTemplateContents(template);
                    component.setComponentType(ProcessingComponentType.EXECUTABLE);
                    component.setVisibility(ProcessingComponentVisibility.SYSTEM);
                    component.setOwner(SystemPrincipal.instance().getName());
                    component.addTags(getOrCreateTag(container.getName()).getText());
                    if (this.componentProvider.get(component.getId(), component.getContainerId()) == null) {
                        this.componentProvider.save(component);
                    } else {
                        this.componentProvider.update(component);
                    }
                } catch (Exception inner) {
                    logger.severe(String.format("Faulty component: %s. Error: %s",
                                                current != null ? current.getId() : "n/a",
                                                inner.getMessage()));
                }
            }
        } catch (Exception outer) {
            logger.severe(String.format("Error occured while registering container applications: %s",
                                        outer.getMessage()));
        }
        return readContainer;
    }

    protected void configureApplication(Application app) {
        String appPath = app.getPath();
        app.setName(app.getName());
        app.setPath(appPath);
    }

    protected abstract String getDescription();

    protected abstract String getPathInContainer();

    protected abstract String getPathInSystem();

    protected abstract String getContainerDescriptorFileName();

    protected abstract String getComponentDescriptorFileName();

    protected abstract String getLogoFileName();
}
