package ro.cs.tao.execution.persistence;

import ro.cs.tao.execution.model.ResourceUsageReport;
import ro.cs.tao.persistence.EntityProvider;

/**
 * Interface for managing and retrieving {@link ResourceUsageReport} entities.
 * Extends the {@link EntityProvider} interface to include basic CRUD operations
 * and adds a method specific to retrieving reports by user identifier.
 */
public interface ResourceUsageReportProvider extends EntityProvider<ResourceUsageReport, Long> {

    /**
     * Retrieves a {@code ResourceUsageReport} associated with the specified user identifier.
     *
     * @param userId the unique identifier of the user for whom the resource usage report is to be retrieved
     * @return the {@code ResourceUsageReport} associated with the provided user identifier,
     *         or {@code null} if no report is found for the specified user
     */
    ResourceUsageReport getByUserId(String userId);

}
