package ro.cs.tao.services.interfaces;

import ro.cs.tao.messaging.TaskProgress;

import java.util.List;

public interface ProgressReportService {

    /**
     * Returns information about current long running tasks.
     * Long running tasks should report their progress via the common message bus
     * and topics matching the pattern "(.+)progress".
     *
     */
    List<TaskProgress> getRunningTasks();

}
