package ro.cs.tao.persistence;

import ro.cs.tao.component.DryRunMetrics;

/**
 * Interface for providing access to and management of {@link DryRunMetrics} entities.
 * This extends the {@link EntityProvider} to include additional methods specific
 * to {@link DryRunMetrics}, enabling retrieval and operations using component-related data.
 */
public interface DryRunMetricsProvider extends EntityProvider<DryRunMetrics, Long>{

    /**
     * Retrieves the {@link DryRunMetrics} instance associated with the specified component ID.
     * This method enables querying dry run metrics by the identifier of the related component.
     *
     * @param componentId the unique identifier of the component for which the dry run metrics
     *                    are to be retrieved. Cannot be null or empty.
     * @return the {@link DryRunMetrics} associated with the given component ID,
     *         or null if no matching metrics are found.
     */
    DryRunMetrics getByComponentId(String componentId);
}
