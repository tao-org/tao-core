package ro.cs.tao.execution.model;

import ro.cs.tao.component.LongIdentifiable;

import java.time.LocalDateTime;

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
