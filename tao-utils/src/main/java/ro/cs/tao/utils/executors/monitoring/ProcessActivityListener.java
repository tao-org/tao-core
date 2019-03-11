package ro.cs.tao.utils.executors.monitoring;

import ro.cs.tao.utils.Triple;

import java.time.LocalDateTime;

public interface ProcessActivityListener {
    void onActivity(Triple<LocalDateTime, Long, Long> record);
}
