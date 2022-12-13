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

import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.Tag;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.component.enums.ProcessingComponentType;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.component.enums.TagType;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ContainerType;
import ro.cs.tao.persistence.ContainerProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.ProcessingComponentProvider;
import ro.cs.tao.persistence.TagProvider;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.utils.JacksonUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class BaseImageInstaller implements DockerImageInstaller {
    //protected static final Set<String> winExtensions = new HashSet<String>() {{ add(".bat"); add(".exe"); }};
    private static final Path dockerPath;
    protected final Logger logger;
    protected final TagProvider tagProvider;
    protected final ContainerProvider containerProvider;
    protected final ProcessingComponentProvider componentProvider;
    private List<Tag> componentTags;

    static {
        dockerPath = findInPath("docker" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
        if (dockerPath == null) {
            Logger.getLogger(BaseImageInstaller.class.getName()).warning("[docker] was not found in system path");
        }
    }

    public BaseImageInstaller() {
        this.tagProvider = SpringContextBridge.services().getService(TagProvider.class);
        this.containerProvider = SpringContextBridge.services().getService(ContainerProvider.class);
        this.componentProvider = SpringContextBridge.services().getService(ProcessingComponentProvider.class);
        this.logger = Logger.getLogger(getClass().getName());
        this.componentTags = this.tagProvider.list();
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
                if ((container = DockerManager.getDockerImage(getContainerName())) == null &&
                    (container = DockerManager.getDockerImage(localRegistry + "/" + getContainerName())) == null) {
                    final String cfgValue = ConfigurationManager.getInstance().getValue("tao.docker.images");
                    if (cfgValue == null) {
                        logger.warning("Invalid path for docker images");
                        return null;
                    }
                    final Path dockerImagesPath = Paths.get(cfgValue);
                    Files.createDirectories(dockerImagesPath);
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
                    Properties properties = new Properties();
                    if (Files.exists(dockerfilePath)) {
                        properties.load(Files.newBufferedReader(dockerfilePath));
                    }
                    if (properties.size() > 0) {
                        container = DockerManager.pullImage(properties.getProperty("docker.hub.name"));
                    } else {
                        dockerfilePath = dockerImagesPath.resolve(getContainerName()).resolve("Dockerfile");
                        if (!Files.exists(dockerfilePath) || Files.size(dockerfilePath) == 0) {
                            Files.deleteIfExists(dockerfilePath);
                            logger.finest(String.format("Extracting Dockerfile for image %s", getContainerName()));
                            //Files.createDirectories(dockerfilePath.getParent());
                            try (BufferedInputStream is = new BufferedInputStream(getClass().getResourceAsStream("Dockerfile"));
                                 OutputStream os = new BufferedOutputStream(Files.newOutputStream(dockerfilePath))) {
                                int read;
                                while ((read = is.read(buffer)) != -1) {
                                    os.write(buffer, 0, read);
                                }
                                os.flush();
                            }
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
            dbContainer = initializeContainer(dbContainer, dockerPath != null ? getPathInContainer() : getPathInSystem());
            if (dbContainer == null) {
                logger.severe(String.format("Container %s failed to register", getContainerName()));
            } else {
                logger.fine(String.format("Container %s registered with id '%s'", getContainerName(), dbContainer.getId()));
            }
        }
        return dbContainer;
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
            components = JacksonUtil.OBJECT_MAPPER.readValue(str, ProcessingComponent[].class);
        }
        return components;
    }

    protected String readContainerLogo(String fileName) throws IOException {
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
                    containerApplications.stream().filter(a -> a.getName().equals(component.getId())).findFirst()
                                         .ifPresent(application -> component.setFileLocation(application.getPath()));
                    List<ParameterDescriptor> parameterDescriptors = component.getParameterDescriptors();
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
                        sources.forEach(s -> s.setId(UUID.randomUUID().toString()));
                    }
                    final List<TargetDescriptor> targets = component.getTargets();
                    if (targets != null) {
                        targets.forEach(t -> t.setId(UUID.randomUUID().toString()));
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
                    /*final String[] tokens = template.split("\n");
                    for (int j = 0; j < tokens.length - 1; j++) {
                        final int idx = j;
                        if ((targets != null && targets.stream().anyMatch(t -> t.getName().equals(tokens[idx].substring(1)))) ||
                                (sources != null && sources.stream().anyMatch(s -> s.getName().equals(tokens[idx].substring(1))))) {
                            tokens[j + 1] = tokens[j].replace('-', '$');
                            j++;
                        }
                    }
                    component.setTemplateContents(String.join("\n", tokens));*/
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

    protected abstract String getContainerName();

    protected abstract String getDescription();

    protected abstract String getPathInContainer();

    protected abstract String getPathInSystem();

    protected abstract String getContainerDescriptorFileName();

    protected abstract String getComponentDescriptorFileName();

    protected abstract String getLogoFileName();
}
