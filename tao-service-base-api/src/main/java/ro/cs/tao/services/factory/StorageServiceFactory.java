package ro.cs.tao.services.factory;

import ro.cs.tao.services.interfaces.StorageService;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.topology.Workspace;

import java.util.Set;

public class StorageServiceFactory {

    public static final StorageService getInstance(Workspace workspace) {
        final ServiceRegistry<StorageService> registry = ServiceRegistryManager.getInstance().getServiceRegistry(StorageService.class);
        final Set<StorageService> services = registry.getServices();
        if (services == null) {
            throw new IllegalArgumentException("No remote file repository is available");
        }
        StorageService service = services.stream().filter(s -> s.isIntendedFor(workspace.getUrlPrefix())).findFirst().orElse(null);
        if (service == null) {
            throw new IllegalArgumentException("No remote file repository for the given protocol is available");
        }
        // Don't forget that a SPI produces a single instance, and we don't want to re-use it
        try {
            service = service.getClass().newInstance();
            service.setup(workspace.getParameters());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return service;
    }
    public static final StorageService getInstance(String protocol) {
        final ServiceRegistry<StorageService> registry = ServiceRegistryManager.getInstance().getServiceRegistry(StorageService.class);
        final Set<StorageService> services = registry.getServices();
        if (services == null) {
            throw new IllegalArgumentException("No remote file repository is available");
        }
        StorageService service = services.stream().filter(s -> s.isIntendedFor(protocol)).findFirst().orElse(null);
        if (service == null) {
            throw new IllegalArgumentException("No remote file repository for the given protocol is available");
        }
        return service;
    }


}
