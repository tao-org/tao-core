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
    private Set<Executor> services;
    private ExecutionsManager() {
        ServiceRegistry<Executor> registry = ServiceRegistryManager.getInstance().getServiceRegistry(Executor.class);
        services = registry.getServices();
        services.forEach(Executor::initialize);
    }

    public static ExecutionsManager getInstance() {
        return instance;
    }

    public void execute(ExecutionTask task) {
        Executor executor = getExecutor(task);
        executor.execute(task);
    }

    public void stop(ExecutionTask task) {
        Executor executor = getExecutor(task);
        executor.stop(task);
    }

    public void suspend(ExecutionTask task) {
        Executor executor = getExecutor(task);
        executor.suspend(task);
    }

    public void resume(ExecutionTask task) {
        Executor executor = getExecutor(task);
        executor.resume(task);
    }

    private Executor getExecutor(ExecutionTask task) {

        Optional<Executor> optional = services.stream()
                .filter(x -> x.supports(task.getProcessingComponent()))
                .findFirst();
        if(optional.isPresent()) {
            return optional.get();
        } else {
            throw new ExecutionException("The component does not have an associated executor!");
        }
    }
}
