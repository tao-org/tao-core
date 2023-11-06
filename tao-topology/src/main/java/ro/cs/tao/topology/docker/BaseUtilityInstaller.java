package ro.cs.tao.topology.docker;

import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ContainerVisibility;
import ro.cs.tao.persistence.ContainerProvider;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.utils.JacksonUtil;

import java.io.*;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class BaseUtilityInstaller implements UtilityContainerInstaller {
    protected final Logger logger;
    protected final ContainerProvider containerProvider;

    public BaseUtilityInstaller() {
        this.containerProvider = SpringContextBridge.services().getService(ContainerProvider.class);
        this.logger = Logger.getLogger(getClass().getName());
    }

    @Override
    public Container install() throws IOException {
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
                    newContainer.setVisibility(ContainerVisibility.PUBLIC);
                    this.containerProvider.save(newContainer);
                }
            }
            return dockerContainer;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new IOException(e);
        }
    }

    protected Container readContainerDescriptor(String fileName) throws IOException {
        Container retVal = null;
        InputStream stream = getClass().getResourceAsStream(fileName);
        if (stream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String str = reader.lines().collect(Collectors.joining(""));
                retVal = JacksonUtil.fromString(str, Container.class);
            } finally {
                stream.close();
            }
        }
        return retVal;
    }

    protected String readContainerLogo(String fileName) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(fileName)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int read;
            byte[] buffer = new byte[1024];
            if (in != null) {
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
                return Base64.getEncoder().encodeToString(out.toByteArray());
            } else {
                return null;
            }
        }
    }

    protected abstract String logoFile();

    protected abstract String descriptor();
}
