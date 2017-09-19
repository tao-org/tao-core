package ro.cs.tao.execution;

import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.util.Optional;
import java.util.Set;

/**
 * Created by cosmin on 9/14/2017.
 */
public class ExecutionsManager {

    private ServiceRegistry registry;
    private Set<IExecutor> services;
    private ExecutionsManager() {
        this.registry = ServiceRegistryManager.getInstance().getServiceRegistry(IExecutor.class);
        services = this.registry.getServices();
    }

    public void executeComponent(TaoComponent component) {
        IExecutor executor = getExecutor(component);
        executor.executeComponent(component);
    }

    public void stopExecution(TaoComponent component) {
        IExecutor executor = getExecutor(component);
        executor.stopExecution(component);
    }

    private IExecutor getExecutor(TaoComponent component) {

        Optional<IExecutor> optional = services.stream()
                .filter(x -> x.supports(component))
                .findFirst();
        if(optional.isPresent()) {
            return optional.get();
        } else {
            throw new ExecutionException("The component does not have an associated executor!");
        }
    }
}
