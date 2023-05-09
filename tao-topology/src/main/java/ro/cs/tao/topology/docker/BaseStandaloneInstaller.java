package ro.cs.tao.topology.docker;

import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ContainerType;
import ro.cs.tao.utils.DockerHelper;

import java.io.IOException;
import java.util.List;

public abstract class BaseStandaloneInstaller extends BaseUtilityInstaller
        implements StandaloneContainer{

    public BaseStandaloneInstaller() {
        super();
    }

    @Override
    public void start() throws IOException {
        if (!DockerHelper.isDockerFound()) {
            throw new IOException("This plugin requires Docker to be installed");
        }
        DockerManager.runDaemon(getContainerName(), startupArguments());
    }

    @Override
    public Container install() throws IOException {
        final Container container = super.install();
        container.setType(ContainerType.UTILITY);
        return container;
    }

    @Override
    public void shutdown() throws IOException {
        if (!DockerHelper.isDockerFound()) {
            throw new IOException("This plugin requires Docker to be installed");
        }
        DockerManager.stopContainer(getContainerName());
    }

    protected abstract List<String> startupArguments();

}
