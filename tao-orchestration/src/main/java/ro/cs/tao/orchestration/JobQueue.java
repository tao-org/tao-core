package ro.cs.tao.orchestration;

import ro.cs.tao.component.SystemVariable;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.utils.FileMap;

import java.nio.file.Paths;
import java.util.*;

/**
 * Simple implementation of a job queue backed by a file.
 *
 * @author Cosmin Cara
 * @since 1.4.3
 */
public class JobQueue {
    private final Map<String, Queue<Long>> queue;
    private final ExecutionJobProvider jobProvider;

    public JobQueue(ExecutionJobProvider jobProvider) {
        this.queue = Collections.synchronizedMap(new FileMap<>(Paths.get(SystemVariable.CACHE.value()).resolve("wait_jobs.json")));
        this.jobProvider = jobProvider;
    }

    /**
     * Initializes the queue with the jobs that have not yet been executed.
     */
    public void initialize() {
        final Set<ExecutionStatus> statuses = new HashSet<>() {{
            add(ExecutionStatus.UNDETERMINED);
            add(ExecutionStatus.QUEUED_ACTIVE);
        }};
        final List<ExecutionJob> runnableJobs = this.jobProvider.list(statuses);
        if (runnableJobs != null) {
            for (ExecutionJob job : runnableJobs) {
                put(job);
            }
        }
    }

    /**
     * Adds a job to the user queue and returns its position in the queue.
     * @param job   The job to be queued
     */
    public int put(ExecutionJob job) {
        final String userName = job.getUserName();
        if (!this.queue.containsKey(userName)) {
            this.queue.put(userName, new ArrayDeque<>());
        }
        final Queue<Long> userQueue = this.queue.get(userName);
        userQueue.add(job.getId());
        this.queue.put(userName, userQueue);
        return userQueue.size();
    }

    public boolean removeJob(ExecutionJob job) {
        final Queue<Long> userQueue = this.queue.get(job.getUserName());
        if (userQueue != null) {
            return userQueue.removeIf(i -> i.equals(job.getId()));
        }
        return false;
    }

    /**
     * Removes all the queued jobs for a user
     * @param user  The name of the user
     */
    public List<Long> removeUserJobs(String user) {
        final Queue<Long> userQueue = this.queue.remove(user);
        return userQueue != null ? new ArrayList<>(userQueue) : null;
    }

    public void moveJobToHead(String user, long jobId) {
        final Queue<Long> userQueue = this.queue.get(user);
        if (userQueue == null || userQueue.size() == 0) {
            throw new IllegalArgumentException("User queue is empty");
        }
        if (!userQueue.peek().equals(jobId)) {
            final ArrayDeque<Long> newQueue = new ArrayDeque<>(userQueue.size());
            while (!userQueue.isEmpty()) {
                final Long element = userQueue.poll();
                if (element != jobId) {
                    newQueue.offer(element);
                } else {
                    final Long previous = newQueue.removeLast();
                    newQueue.offer(jobId);
                    newQueue.offer(previous);
                }
            }
            this.queue.put(user, newQueue);
        }
    }

    public void moveJobToTail(String user, long jobId) {
        final Queue<Long> userQueue = this.queue.get(user);
        if (userQueue == null || userQueue.size() == 0) {
            throw new IllegalArgumentException("User queue is empty");
        }
        if (!userQueue.peek().equals(jobId)) {
            final ArrayDeque<Long> newQueue = new ArrayDeque<>(userQueue.size());
            boolean found = false;
            while (!userQueue.isEmpty()) {
                final Long element = userQueue.poll();
                if (element != jobId) {
                    newQueue.offer(element);
                    if (found) {
                        newQueue.offer(jobId);
                    }
                } else {
                    found = true;
                }
            }
            this.queue.put(user, newQueue);
        }
    }

    public void removeJob(String user, long jobId) {
        final Queue<Long> userQueue = this.queue.get(user);
        if (userQueue != null) {
            final Iterator<Long> iterator = userQueue.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().equals(jobId)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    /**
     * Lists all the queued jobs of a user
     * @param user  The name of the user
     */
    public List<Long> getUserJobs(String user) {
        final Queue<Long> userQueue = this.queue.get(user);
        return userQueue != null ? new ArrayList<>(userQueue) : null;
    }

    /**
     * Lists all the jobs for all the users, grouped by user
     */
    public Map<String, Queue<Long>> getAll() {
        return new LinkedHashMap<>(this.queue);
    }

    /**
     * Retrieves the job that on the head of the user queue.
     * @param user  The name of the user
     */
    public ExecutionJob take(String user) {
        final Queue<Long> userQueue = this.queue.get(user);
        final ExecutionJob job;
        if (userQueue != null && !userQueue.isEmpty()) {
            job = this.jobProvider.get(userQueue.poll());
            if (userQueue.isEmpty()) {
                this.queue.remove(user);
            }
        } else {
            job = null;
        }
        return job;
    }
}
