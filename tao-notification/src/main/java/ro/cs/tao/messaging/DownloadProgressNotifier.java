package ro.cs.tao.messaging;

import ro.cs.tao.messaging.progress.DownloadProgress;
import ro.cs.tao.utils.executors.monitoring.DownloadProgressListener;

import java.security.Principal;
import java.util.Map;

public class DownloadProgressNotifier extends ProgressNotifier implements DownloadProgressListener {
    public DownloadProgressNotifier(Object source, Topic topic) {
        super(source, topic);
    }

    public DownloadProgressNotifier(Principal principal, Object source, Topic topic) {
        super(principal, source, topic);
    }

    public DownloadProgressNotifier(Principal principal, Object source, Topic topic, Map<String, String> additionalInfo) {
        super(principal, source, topic, additionalInfo);
    }

    @Override
    public void notifyProgress(double progressValue, double transferSpeed) {
        taskCounter = progressValue;
        if (taskCounter < 1) {
            sendTransientMessage(new DownloadProgress(taskName, progressValue, transferSpeed));
        } else {
            ended();
        }
    }
}
