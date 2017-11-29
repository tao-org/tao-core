package ro.cs.tao.services.model.monitoring;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author Cosmin Cara
 */
public class Runtime {

    private TimeUnit timeUnit;

    private LocalDateTime startTime;
    private long upTime;
    private long currentThreadCpuTime;
    private long currentThreadUserTime;
    private int daemonThreadCount;
    private int peakThreadCount;
    private int threadCount;
    private int availableProcessors;
    private double lastMinuteSystemLoad;

    public Runtime(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
    }

    /**
     * Up-time in seconds
     */
    public long getUpTime() {
        return upTime / this.timeUnit.value();
    }

    public void setUpTime(long upTime) {
        this.upTime = upTime;
    }

    /**
     * Current thread CPU time in seconds
     */
    public long getCurrentThreadCpuTime() {
        return currentThreadCpuTime / this.timeUnit.value();
    }

    public void setCurrentThreadCpuTime(long currentThreadCpuTime) {
        this.currentThreadCpuTime = currentThreadCpuTime;
    }

    public long getCurrentThreadUserTime() {
        return currentThreadUserTime / this.timeUnit.value();
    }

    public void setCurrentThreadUserTime(long currentThreadUserTime) {
        this.currentThreadUserTime = currentThreadUserTime;
    }

    public int getDaemonThreadCount() {
        return daemonThreadCount;
    }

    public void setDaemonThreadCount(int daemonThreadCount) {
        this.daemonThreadCount = daemonThreadCount;
    }

    public int getPeakThreadCount() {
        return peakThreadCount;
    }

    public void setPeakThreadCount(int peakThreadCount) {
        this.peakThreadCount = peakThreadCount;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public double getLastMinuteSystemLoad() {
        return lastMinuteSystemLoad;
    }

    public void setLastMinuteSystemLoad(double lastMinuteSystemLoad) {
        this.lastMinuteSystemLoad = lastMinuteSystemLoad;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public void setAvailableProcessors(int availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

}
