package ro.cs.tao.execution;

import ro.cs.tao.component.execution.ExecutionTask;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.util.Optional;
import java.util.Set;

/**
 * Created by cosmin on 9/14/2017.
 */
public class ExecutionsManager {

    private static ExecutionsManager instance = new ExecutionsManager();
    private ServiceRegistry<IExecutor> registry;
    private Set<IExecutor> services;
    private ExecutionsManager() {
        this.registry = ServiceRegistryManager.getInstance().getServiceRegistry(IExecutor.class);
        services = this.registry.getServices();
        services.stream().forEach(x -> x.initialize());
    }

    public static ExecutionsManager getInstance() {
        return instance;
    }

    public void execute(ExecutionTask task) {
        IExecutor executor = getExecutor(task);
        executor.execute(task);
    }

    public void stop(ExecutionTask task) {
        IExecutor executor = getExecutor(task);
        executor.stop(task);
    }

    private IExecutor getExecutor(ExecutionTask task) {

        Optional<IExecutor> optional = services.stream()
                .filter(x -> x.supports(task.getProcessingComponent()))
                .findFirst();
        if(optional.isPresent()) {
            return optional.get();
        } else {
            throw new ExecutionException("The component does not have an associated executor!");
        }
    }
}
