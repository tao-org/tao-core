package ro.cs.tao.utils.executors;

import ro.cs.tao.utils.Triple;

import java.time.LocalDateTime;
import java.util.*;

public class ActivityListener {
    private int sampleInterval;
    private final List<Triple<LocalDateTime, Long, Long>> records;
    private final Map<Process, ProcessActivityMonitor> monitorMap;

    public ActivityListener() {
        this.monitorMap = new HashMap<>();
        this.records = new ArrayList<>();
        this.sampleInterval = 10000;
    }

    public void setSampleInterval(int sampleInterval) { this.sampleInterval = sampleInterval; }

    void attachTo(Process process) {
        if (!this.monitorMap.containsKey(process)) {
            this.monitorMap.put(process,
                                ProcessActivityMonitor.createMonitor(process, this.sampleInterval));
        }
    }

    void detachFrom(Process process) {
        if (this.monitorMap.containsKey(process)) {
            this.records.addAll(this.monitorMap.get(process).getRecords());
            this.monitorMap.remove(process);
        }
    }

    public List<Triple<LocalDateTime, Long, Long>> getRecords() {
        Set<Process> processes = this.monitorMap.keySet();
        for (Process process : processes) {
            this.records.addAll(this.monitorMap.get(process).getRecords());
        }
        this.records.sort(Comparator.comparing(Triple::getKeyOne));
        return this.records;
    }
}
