package ro.cs.tao.spi;

import ro.cs.tao.lifecycle.ComponentLifeCycle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is responsible with the invocation of (possible) component activator invocations.
 *
 * @author Cosmin Cara
 */
public class LifeCycleProcessor {

    private static final ExecutorService executor;
    private static final List<ComponentLifeCycle> detectedComponents;

    static {
        detectedComponents = new ArrayList<>();
        executor = Executors.newCachedThreadPool();
        Runtime.getRuntime().addShutdownHook(new Thread(LifeCycleProcessor::onShutdown));
    }

    public static void activate() {
        final ServiceRegistry<ComponentLifeCycle> registry = ServiceRegistryManager.getInstance().getServiceRegistry(ComponentLifeCycle.class);
        if (registry != null) {
            detectedComponents.addAll(registry.getServices());
        }
        onStartUp();
    }

    private static void onStartUp() {
        detectedComponents.forEach(c -> {
            executor.submit(() -> {
                try {
                    c.onStartUp();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private static void onShutdown() {
        detectedComponents.forEach(c -> {
            executor.submit(() -> {
                try {
                    c.onShutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
