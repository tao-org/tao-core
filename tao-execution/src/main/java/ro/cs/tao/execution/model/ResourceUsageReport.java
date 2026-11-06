package ro.cs.tao.execution.model;

import ro.cs.tao.component.LongIdentifiable;

import java.time.LocalDateTime;

/**
 * Represents a report that encapsulates the resource usage information
 * associated with a specific user. This class extends {@code LongIdentifiable},
 * which provides an identifier of type {@code Long} for the object.
 *
 * The class contains the following details:
 * - The user identifier associated with the resource usage.
 * - The timestamp indicating when the last report was generated.
 */
public class ResourceUsageReport extends LongIdentifiable {
    private String userId;
    private LocalDateTime lastReportTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getLastReportTime() {
        return lastReportTime;
    }

    public void setLastReportTime(LocalDateTime lastReportTime) {
        this.lastReportTime = lastReportTime;
    }
}
