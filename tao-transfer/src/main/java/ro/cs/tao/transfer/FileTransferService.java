package ro.cs.tao.transfer;

import ro.cs.tao.docker.ExecutionConfiguration;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.utils.executors.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Utility service that allows transferring files to an execution node from a shared folder.
 * The transfer is initiated by the execution node.
 *
 * @author Cosmin Cara
 * @since 1.1.0
 */
public class FileTransferService {
    private static final String commandTemplate = "cp -t %s %s{%s}";
    private static final int TIMEOUT = 300;
    private static final FileTransferService instance;
    private final BlockingQueue<TransferRequest> queue;
    private final ExecutorService asyncCallbackWorker;
    private final Set<Long> pendingTasks;
    private final List<FileTransferCallback> listeners;

    static {
        instance = new FileTransferService();
    }

    private FileTransferService() {
        this.queue = new LinkedBlockingDeque<>();
        BlockingQueueWorker<TransferRequest> queueWorker = new BlockingQueueWorker<>(this.queue, this::processRequest);
        this.pendingTasks = Collections.synchronizedSet(new HashSet<>());
        this.listeners = new ArrayList<>();
        this.asyncCallbackWorker = Executors.newSingleThreadExecutor();
        queueWorker.start();
    }

    /**
     * Register a listener to be notified when the transfer completes.
     * @param callback  The listener
     */
    public void addListener(FileTransferCallback callback) {
        this.listeners.add(callback);
    }

    /**
     * Returns true if a transfer was already requested for the given execution task.
     * @param task  The execution task
     */
    public boolean hasPendingJob(ExecutionTask task) {
        return this.pendingTasks.contains(task.getId());
    }

    /**
     * Submits a transfer request to the service queue.
     * @param request   The request
     */
    public void request(TransferRequest request) {
        try {
            this.queue.put(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Void processRequest(TransferRequest item) {
        this.pendingTasks.add(item.getTaskId());
        final Set<String> sourceFiles = item.getSourceFiles();
        String target = item.getTarget();
        if (!target.endsWith("/")) {
            target += "/";
        }
        final String sourceRoot = getCommonPath(sourceFiles);
        final int size = sourceFiles.size();
        final String[] values = new String[size];
        int idx = 0;
        for (String srcFile : sourceFiles) {
            values[idx++] = srcFile.replace(sourceRoot, "");
        }
        List<String> args = Arrays.asList(String.format(commandTemplate,
                                                        target,
                                                        sourceRoot,
                                                        String.join(",", values))
                                                .split(" "));
        final ExecutionUnit unit;
        final NodeDescription node = item.getTargetHost();
        final String nodeName = node.getId();
        if (ExecutionConfiguration.developmentModeEnabled()) {
            unit = new ExecutionUnit(ExecutorType.MOCK, nodeName, node.getUserName(), node.getUserPass(),
                    args, false, SSHMode.EXEC);
        } else {
            unit = isLocalHost(nodeName) ?
                    new ExecutionUnit(ExecutorType.PROCESS, nodeName, node.getUserName(), node.getUserPass(),
                            args, false, null) :
                    new ExecutionUnit(ExecutorType.SSH2, nodeName, node.getUserName(), node.getUserPass(),
                            args, false, SSHMode.EXEC);
        }
        final String finalTarget = target;
        this.asyncCallbackWorker.execute(() -> {
            try {
                final OutputAccumulator consumer = new OutputAccumulator();
                if (Executor.execute(consumer, TIMEOUT, unit) != 0) {
                    System.err.printf("Transfer to %s failed: %s%n", nodeName, consumer.getOutput());
                } else {
                    final Map<String, String> files = new LinkedHashMap<>();
                    for (String srcFile : sourceFiles) {
                        files.put(srcFile, srcFile.replace(sourceRoot, finalTarget));
                    }
                    notifyListeners(item.getTaskId(), files);
                }
            } finally {
                pendingTasks.remove(item.getTaskId());
            }
        });
        return null;
    }

    private String getCommonPath(Set<String> paths) {
        String commonPart = "";
        int min = 0;
        for (String path : paths) {
            if (min == 0) {
                commonPart = path;
                min = commonPart.length();
                continue;
            }
            min = Math.min(min, path.length());
            for (int i = 0; i < min; i++) {
                if (path.charAt(i) != commonPart.charAt(i)) {
                    min = i;
                    break;
                }
            }
        }
        return commonPart.substring(0, Math.min(min, commonPart.lastIndexOf('/')));
    }

    private boolean isLocalHost(String host) {
        try {
            return host.equalsIgnoreCase(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void notifyListeners(long taskId, Map<String, String> transferredFiles) {
        for (FileTransferCallback callback : this.listeners) {
            callback.onCompleted(taskId, transferredFiles);
        }
    }
}
