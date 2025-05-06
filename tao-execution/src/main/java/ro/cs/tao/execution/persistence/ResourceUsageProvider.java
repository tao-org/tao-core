package ro.cs.tao.execution.persistence;

import ro.cs.tao.execution.model.ResourceUsage;
import ro.cs.tao.persistence.EntityProvider;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Provides methods to manage and retrieve {@link ResourceUsage} entities.
 * Extends the {@link EntityProvider} interface to offer basic CRUD operations
 * and includes additional methods tailored for resource usage-related queries.
 */
public interface ResourceUsageProvider extends EntityProvider<ResourceUsage, Long> {

    /**
     * Retrieves a list of {@code ResourceUsage} instances associated with the specified user.
     *
     * @param userId the unique identifier of the user whose resource usage records are to be retrieved
     * @return a list of {@code ResourceUsage} objects associated with the specified user
     */
    List<ResourceUsage> getByUserId(String userId);

    /**
     * Retrieves a list of {@code ResourceUsage} instances associated with the specified user
     * and falling within the given time interval.
     *
     * @param user      the unique identifier of the user whose resource usage records are to be retrieved
     * @param startDate the starting point of the time interval
     * @param endDate   the ending point of the time interval
     * @return a list of {@code ResourceUsage} objects associated with the specified user
     *         and within the provided time interval
     */
    List<ResourceUsage> getByUserIdAndInterval(String user, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Retrieves a list of {@code ResourceUsage} instances associated with the specified user
     * and occurring since the provided date and time.
     *
     * @param user the unique identifier of the user whose resource usage records are to be retrieved
     * @param date the starting {@code LocalDateTime} from which resource usage records should be considered
     * @return a list of {@code ResourceUsage} objects associated with the specified user
     *         and occurring on or after the provided date and time
     */
    List<ResourceUsage> getByUserIdSince(String user, LocalDateTime date);

    /**
     * Retrieves the {@code ResourceUsage} instance associated with the specified task.
     *
     * @param taskId the unique identifier of the task whose resource usage is to be retrieved
     * @return the {@code ResourceUsage} object associated with the specified task, or {@code null} if no match is found
     */
    ResourceUsage getByTask(long taskId);
}
