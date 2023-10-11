package ro.cs.tao.execution.model;

import ro.cs.tao.component.LongIdentifiable;

import java.time.LocalDateTime;

public class ResourceUsage extends LongIdentifiable {
    private String host;
    private String flavor;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
