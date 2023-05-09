package ro.cs.tao;

import ro.cs.tao.component.AggregationException;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.RuntimeOptimizer;

import java.util.List;
import java.util.Map;

/**
 * Base class for runtime optimizers.
 *
 * @author Cosmin Cara
 */
public abstract class BaseRuntimeOptimizer implements RuntimeOptimizer {

    @Override
    public ProcessingComponent aggregate(List<ProcessingComponent> sources,
                                         Map<String, Map<String, String>> values) throws AggregationException {
        ProcessingComponent aggregator = null;
        if (sources != null && sources.size() > 0) {
            if (sources.size() == 1) {
                aggregator = sources.get(0);
            } else {
                String containerId = null;
                for (ProcessingComponent source : sources) {
                    if (!isIntendedFor(source.getContainerId())) {
                        throw new AggregationException(String.format("This aggregator is not intended for components belonging to the container '%s'",
                                                                     source.getContainerId()));
                    } else if (containerId == null) {
                        containerId = source.getContainerId();
                    } else if (!containerId.equals(source.getContainerId())) {
                        throw new AggregationException("The components to be aggregated must belong to the same container");
                    }
                }
                aggregator = createAggregatedComponent(sources, values);
            }
        }
        return aggregator;
    }

    /**
     * Handles the actual creation of the aggregated component from the given source components
     * @param sources   The processing components to be joined
     */
    protected abstract ProcessingComponent createAggregatedComponent(List<ProcessingComponent> sources,
                                                                     Map<String, Map<String, String>> values) throws AggregationException;
}
