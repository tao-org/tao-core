package ro.cs.tao.execution.persistence;

import ro.cs.tao.execution.model.ResourceUsage;
import ro.cs.tao.persistence.EntityProvider;

import java.time.LocalDateTime;
import java.util.List;

public interface ResourceUsageProvider extends EntityProvider<ResourceUsage, Long> {

    List<ResourceUsage> getByUserId(String userId);

    List<ResourceUsage> getByUserIdAndInterval(String user, LocalDateTime startDate, LocalDateTime endDate);

    List<ResourceUsage> getByUserIdSince(String user, LocalDateTime date);

    ResourceUsage getByTask(long taskId);
}
