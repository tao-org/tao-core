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
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.enums.TagType;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.docker.Container;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.data.jsonutil.JacksonUtil;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.topology.TopologyManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class BaseImageInstaller implements DockerImageInstaller {
    protected static final Set<String> winExtensions = new HashSet<String>() {{ add(".bat"); add(".exe"); }};
    private static final Path dockerPath;
    protected final Logger logger;
    private List<Tag> componentTags;
    private final PersistenceManager persistenceManager;

    static {
        dockerPath = findInPath("docker" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
        if (dockerPath == null) {
            Logger.getLogger(BaseImageInstaller.class.getName()).warning("[docker] was not found in system path");
        }
    }

    public BaseImageInstaller() {
        this.persistenceManager = SpringContextBridge.services().getService(PersistenceManager.class);
        this.logger = Logger.getLogger(getClass().getName());
        this.componentTags = this.persistenceManager.getComponentTags();
        if (this.componentTags == null) {
            this.componentTags = new ArrayList<>();
        }
    }

    public PersistenceManager getPersistenceManager() { return persistenceManager; }

    @Override
    public Container installImage() {
        Container container = null;
        if (dockerPath != null) {
            try {
                Path dockerImagesPath = Paths.get(ConfigurationManager.getInstance().getValue("tao.docker.images"));
                if (dockerImagesPath == null) {
                    logger.warning("Invalid path for docker images");
                    return null;
                }
                Files.createDirectories(dockerImagesPath);
                Path dockerfilePath = dockerImagesPath.resolve(getContainerName()).resolve("Dockerfile");
                if (!Files.exists(dockerfilePath)) {
                    logger.finest(String.format("Extracting Dockerfile for image %s", getContainerName()));
                    Files.createDirectories(dockerfilePath.getParent());
                    byte[] buffer = new byte[1024];
                    try (BufferedInputStream is = new BufferedInputStream(getClass().getResourceAsStream("Dockerfile"));
                         OutputStream os = new BufferedOutputStream(Files.newOutputStream(dockerfilePath))) {
                        int read;
                        while ((read = is.read(buffer)) != -1) {
                            os.write(buffer, 0, read);
                        }
                        os.flush();
                    }
                }
                TopologyManager topologyManager = TopologyManager.getInstance();
                container = topologyManager.getDockerImage(getContainerName());
                if (container == null) {
                    this.logger.info(String.format("Image %s was not found in Docker registry. Registration starting.\n" +
                                                             "Until registration completes, the corresponding components will not be available.", getContainerName()));
                    topologyManager.registerImage(dockerfilePath.toRealPath(), getContainerName(), getDescription());
                    this.logger.info(String.format("Registration completed for docker image %s.", getContainerName()));
                    container = topologyManager.getDockerImage(getContainerName());
                } else {
                    logger.finest(String.format("Image %s was found in Docker registry", getContainerName()));
                }
            } catch (IOException e) {
                logger.warning(String.format("Error occurred while registering %s: %s",
                                             getContainerName(), e.getMessage()));
            }
        }
        Container dbContainer = persistenceManager.getContainerById(container != null ? container.getId() : getContainerName());
        if (dbContainer == null) {
            logger.info(String.format("Container %s not registered in database, will create one", getContainerName()));
            dbContainer = new Container();
            dbContainer.setId(container != null ? container.getId() : getContainerName());
            dbContainer.setName(getContainerName());
            dbContainer.setTag(container != null ? container.getTag() : null);
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName)))) {
            String str = String.join("", reader.lines().collect(Collectors.toList()));
            return JacksonUtil.fromString(str, Container.class);
        }
    }

    protected ProcessingComponent[] readComponentDescriptors(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName)))) {
            String str2 = String.join("", reader.lines().collect(Collectors.toList()));
            return JacksonUtil.OBJECT_MAPPER.readValue(str2, ProcessingComponent[].class);
        }
    }

    protected String readContainerLogo(String fileName) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(fileName)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int read;
            byte[] buffer = new byte[1024];
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            return Base64.getEncoder().encodeToString(out.toByteArray());
        }
    }

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

    protected Tag getOrCreateTag(String tagText) {
        Tag tag = this.componentTags.stream().filter(t -> t.getText().equalsIgnoreCase(tagText)).findFirst().orElse(null);
        if (tag == null) {
            tag = this.persistenceManager.saveTag(new Tag(TagType.COMPONENT, tagText));
            this.componentTags.add(tag);
        }
        return tag;
    }

    protected abstract String getContainerName();

    protected abstract String getDescription();

    protected abstract String getPathInContainer();

    protected abstract String getPathInSystem();

    protected abstract Container initializeContainer(Container container, String path);
}
