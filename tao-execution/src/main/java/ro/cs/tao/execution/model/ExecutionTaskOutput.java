package ro.cs.tao.execution.model;

import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.utils.executors.MemoryUnit;

import java.time.LocalDateTime;

public class ExecutionTaskOutput {
    private long jobId;
    private long taskId;
    private LocalDateTime endTime;
    private String name;
    private String path;
    private DataFormat format;
    private long size;
    private MemoryUnit unit;

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public DataFormat getFormat() {
        return format;
    }

    public void setFormat(DataFormat format) {
        this.format = format;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public MemoryUnit getUnit() {
        return unit;
    }

    public void setUnit(MemoryUnit unit) {
        this.unit = unit;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }
}
