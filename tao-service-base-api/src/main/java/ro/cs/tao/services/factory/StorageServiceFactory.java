package ro.cs.tao.services.factory;

import ro.cs.tao.persistence.AuxiliaryDataProvider;
import ro.cs.tao.persistence.EOProductProvider;
import ro.cs.tao.persistence.VectorDataProvider;
import ro.cs.tao.services.interfaces.StorageService;
import ro.cs.tao.services.model.ItemAction;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.workspaces.Repository;
import ro.cs.tao.workspaces.RepositoryType;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Factory class for getting instances of storage service implementation
 *
 * @author Cosmin Cara
 */
public class StorageServiceFactory {
    private static EOProductProvider productProvider;
    private static VectorDataProvider vectorDataProvider;
    private static AuxiliaryDataProvider auxiliaryDataProvider;

    private final static Map<Repository, WeakReference<StorageService>> cachedServices = Collections.synchronizedMap(new HashMap<>());
    private final static Map<RepositoryType, List<ItemAction>> cachedActions = new HashMap<>();

    public static void setProductProvider(EOProductProvider productProvider) {
        StorageServiceFactory.productProvider = productProvider;
    }

    public static void setVectorDataProvider(VectorDataProvider vectorDataProvider) {
        StorageServiceFactory.vectorDataProvider = vectorDataProvider;
    }

    public static void setAuxiliaryDataProvider(AuxiliaryDataProvider auxiliaryDataProvider) {
        StorageServiceFactory.auxiliaryDataProvider = auxiliaryDataProvider;
    }

    /**
     * Gets an instance of the storage service associated with the given repository.
     * If no such instance, it returns null.
     * The returned instance is associated with the given repository instance and should not be used with other
     * repository instance.
     *
     * @param workspace     The repository (workspace)
     */
    public static final StorageService getInstance(Repository workspace) {
        StorageService service;
        if (!cachedServices.containsKey(workspace) || (service = cachedServices.get(workspace).get()) == null) {
            final ServiceRegistry<StorageService> registry = ServiceRegistryManager.getInstance().getServiceRegistry(StorageService.class);
            final Set<StorageService> services = registry.getServices();
            if (services == null) {
                throw new IllegalArgumentException("No remote file repository is available");
            }
            service = services.stream().filter(s -> s.isIntendedFor(workspace.getUrlPrefix())).findFirst().orElse(null);
            if (service == null) {
                throw new IllegalArgumentException("No remote file repository for the given protocol is available");
            }
            RepositoryType type = workspace.getType();
            if (!cachedActions.containsKey(type)) {
                final ServiceRegistry<ItemAction> actionRegistry = ServiceRegistryManager.getInstance().getServiceRegistry(ItemAction.class);
                if (actionRegistry != null) {
                    final Set<ItemAction> actions = actionRegistry.getServices();
                    if (actions != null) {
                        cachedActions.put(type, new ArrayList<>());
                        for (ItemAction action : actions) {
                            if (action.isIntendedFor(type)) {
                                cachedActions.get(type).add(action);
                            }
                        }
                    }
                }
            }
            // Don't forget that a SPI produces a single instance, and we don't want to re-use it
            try {
                service = service.getClass().newInstance();
                service.associate(workspace);
                service.setProductProvider(productProvider);
                service.setVectorDataProvider(vectorDataProvider);
                service.setAuxiliaryDataProvider(auxiliaryDataProvider);
                final List<ItemAction> itemActions = cachedActions.get(type);
                if (itemActions != null) {
                    for (ItemAction action : itemActions) {
                        service.registerAction(action);
                    }
                }
                cachedServices.put(workspace, new WeakReference<>(service));
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return service;
    }

    /**
     * Gets a storage service instance for the given protocol.
     * If multiple storage services are defined for the same protocol, the first one is returned (no predictable order)
     * @param protocol  The protocol
     */
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
        try {
            service = service.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return service;
    }


}
