package ro.cs.tao.topology.docker;

import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ContainerType;
import ro.cs.tao.docker.ContainerVisibility;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.utils.DockerHelper;

import java.io.IOException;
import java.util.List;

public abstract class BaseSingletonInstaller extends BaseUtilityInstaller
        implements SingletonContainer {

    public BaseSingletonInstaller() {
        super();
    }

    @Override
    public String start() throws IOException {
        if (!DockerHelper.isDockerFound()) {
            throw new IOException("This plugin requires Docker to be installed");
        }
        final String name = SystemPrincipal.instance().getName();
        return DockerManager.runDaemon(getContainerName(), name, null, name, startupArguments());
    }

    @Override
    public Container install() throws IOException {
        final Container container = super.install();
        if (container != null) {
            container.setType(ContainerType.UTILITY);
            container.setVisibility(ContainerVisibility.PUBLIC);
        }
        return container;
    }

    @Override
    public void shutdown() throws IOException {
        if (!DockerHelper.isDockerFound()) {
            throw new IOException("This plugin requires Docker to be installed");
        }
        DockerManager.stopInstances(getContainerName());
    }

    protected abstract List<String> startupArguments();

}
