package ro.cs.tao.topology.docker;

import ro.cs.tao.docker.Container;
import ro.cs.tao.persistence.ContainerProvider;
import ro.cs.tao.persistence.data.jsonutil.JacksonUtil;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.utils.DockerHelper;

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class BaseStandaloneContainer implements StandaloneContainer {
    protected final Logger logger;
    protected final ContainerProvider containerProvider;

    public BaseStandaloneContainer() {
        this.containerProvider = SpringContextBridge.services().getService(ContainerProvider.class);
        this.logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public void install() throws IOException {
        //docker pull spali/shellinabox
        try {
            final Container dockerContainer = DockerManager.pullImage(getContainerName());
            if (dockerContainer != null) {
                final Container dbContainer = this.containerProvider.get(dockerContainer.getId());
                if (dbContainer == null) {
                    final Container newContainer = readContainerDescriptor(descriptor());
                    newContainer.setId(dockerContainer.getId());
                    newContainer.setName(dockerContainer.getName());
                    newContainer.setTag(dockerContainer.getTag());
                    newContainer.setLogo(readContainerLogo(logoFile()));
                    this.containerProvider.save(newContainer);
                }
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new IOException(e);
        }
    }

    @Override
    public void start() throws IOException {
        if (!DockerHelper.isDockerFound()) {
            throw new IOException("This plugin requires Docker to be installed");
        }
        DockerManager.runDaemon(getContainerName(), startupArguments());
    }

    @Override
    public void shutdown() throws IOException {
        if (!DockerHelper.isDockerFound()) {
            throw new IOException("This plugin requires Docker to be installed");
        }
        DockerManager.stopContainer(getContainerName());
    }

    protected Container readContainerDescriptor(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName)))) {
            String str = reader.lines().collect(Collectors.joining(""));
            return JacksonUtil.fromString(str, Container.class);
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

    protected abstract String logoFile();

    protected abstract String descriptor();

    protected abstract List<String> startupArguments();

}
