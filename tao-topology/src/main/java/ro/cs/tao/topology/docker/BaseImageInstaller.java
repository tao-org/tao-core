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

package ro.cs.tao.topology.docker;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.docker.Container;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.data.jsonutil.JacksonUtil;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.topology.TopologyManager;
import ro.cs.tao.utils.Platform;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class BaseImageInstaller implements DockerImageInstaller {
    protected static final Set<String> winExtensions = new HashSet<String>() {{ add(".bat"); add(".exe"); }};
    protected final Logger logger;
    private final PersistenceManager persistenceManager;

    public BaseImageInstaller() {
        this.persistenceManager = SpringContextBridge.services().getPersistenceManager();
        this.logger = Logger.getLogger(getClass().getName());
    }

    public PersistenceManager getPersistenceManager() { return persistenceManager; }

    @Override
    public void installImage() {
        Path dockerPath = findInPath("docker" + (Platform.ID.win.equals(Platform.getCurrentPlatform().getId()) ? ".exe" : ""));
        Container container = null;
        if (dockerPath != null) {
            try {
                Path dockerImagesPath = Paths.get(ConfigurationManager.getInstance().getValue("tao.docker.images"));
                Files.createDirectories(dockerImagesPath);
                Path dockerfilePath = dockerImagesPath.resolve(getContainerName()).resolve("Dockerfile");
                if (!Files.exists(dockerfilePath)) {
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
                if (topologyManager.getDockerImage(getContainerName()) == null) {
                    this.logger.info(String.format("Begin registering docker image %s", getContainerName()));
                    topologyManager.registerImage(dockerfilePath.toRealPath(), getContainerName(), getDescription());
                    this.logger.info(String.format("Docker image %s registration completed", getContainerName()));
                }
                container = topologyManager.getDockerImage(getContainerName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            container = persistenceManager.getContainerById(getContainerName());
        } catch (PersistenceException ignored) {
        }
        if (container == null) {
            container = new Container();
            container.setId(getContainerName());
            container.setName(getContainerName());
        }
        container = initializeContainer(container.getId(), dockerPath != null ? getPathInContainer() : getPathInSystem());
        logger.info(String.format("Registered TAO container %s", getContainerName()));
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

    protected Path findInPath(String executableName) {
        String systemPath = System.getenv("Path");
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

    protected abstract String getContainerName();

    protected abstract String getDescription();

    protected abstract String getPathInContainer();

    protected abstract String getPathInSystem();

    protected abstract Container initializeContainer(String containerId, String path);
}
