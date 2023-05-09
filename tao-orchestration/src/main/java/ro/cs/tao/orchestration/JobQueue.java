package ro.cs.tao.orchestration;

import ro.cs.tao.component.SystemVariable;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.JobSelector;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.utils.FileQueue;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.utils.Tuple;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple implementation of a job queue backed by a file.
 *
 * @author Cosmin Cara
 * @since 1.4.3
 */
public class JobQueue {
    private final Queue<Tuple<Long, String>> queue;
    private final ExecutionJobProvider jobProvider;
    private JobSelector<Tuple<Long, String>> jobSelector;
    private final Object lock;

    public JobQueue(ExecutionJobProvider jobProvider) {
        this.queue = new FileQueue<>(Paths.get(SystemVariable.CACHE.value()).resolve("wait_jobs.json"));
        this.jobProvider = jobProvider;
        this.lock = new Object();
        String jobSelectorClass = ConfigurationManager.getInstance().getValue("job.selector");
        try {
            if (!StringUtilities.isNullOrEmpty(jobSelectorClass)) {
                final Class<?> clazz = Class.forName(jobSelectorClass);
                if (JobSelector.class.isAssignableFrom(clazz)) {
                    this.jobSelector = (JobSelector<Tuple<Long, String>>) clazz.getConstructor(queue.getClass()).newInstance(this.queue);
                }
            }
            if (this.jobSelector == null) {
                this.jobSelector = new DefaultJobSelector(this.queue);
            }
        } catch (Exception e) {
            jobSelector = new DefaultJobSelector(this.queue);
        }
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
        synchronized (lock) {
            this.queue.offer(new Tuple<>(job.getId(), job.getUserName()));
            return this.queue.size();
        }
    }

    /**
     * Removes a job from the queue, regardless its position in the queue.
     *
     * @param job  The job object
     */
    public boolean removeJob(ExecutionJob job) {
        synchronized (lock) {
            return this.queue.removeIf(p -> p.getKeyOne().equals(job.getId()) &&
                                            p.getKeyTwo().equals(job.getUserName()));
        }
    }

    /**
     * Removes a job from the queue, regardless its position in the queue.
     *
     * @param user  The user
     * @param jobId The job identifier
     */
    public boolean removeJob(String user, long jobId) {
        synchronized (lock) {
            return queue.removeIf(p -> p.getKeyOne().equals(jobId) && p.getKeyTwo().equals(user));
        }
    }

    /**
     * Removes all the queued jobs for a user
     * @param user  The name of the user
     */
    public List<Long> removeUserJobs(String user) {
        synchronized (lock) {
            List<Long> removedJobs = null;
            final Iterator<Tuple<Long, String>> iterator = this.queue.iterator();
            while (iterator.hasNext()) {
                final Tuple<Long, String> next = iterator.next();
                if (next.getKeyTwo().equals(user)) {
                    iterator.remove();
                    if (removedJobs == null) {
                        removedJobs = new ArrayList<>();
                    }
                    removedJobs.add(next.getKeyOne());
                }
            }
            return removedJobs;
        }
    }

    /**
     * Moves a job one position closer to the head of the queue, regardless the user.
     * If the job is already at the head of the queue, the method does nothing.
     *
     * @param jobId The job identifier
     */
    public void moveJobToHead(long jobId) {
        synchronized (lock) {
            if (!this.queue.isEmpty() && !this.queue.peek().getKeyOne().equals(jobId)) {
                final ArrayDeque<Tuple<Long, String>> newQueue = new ArrayDeque<>(this.queue.size());
                while (!this.queue.isEmpty()) {
                    final Tuple<Long, String> pair = this.queue.poll();
                    if (pair.getKeyOne() != jobId) {
                        newQueue.offer(pair);
                    } else {
                        final Tuple<Long, String> previous = newQueue.removeLast();
                        newQueue.offer(pair);
                        newQueue.offer(previous);
                    }
                }
                this.queue.addAll(newQueue);
            }
        }
    }
    /**
     * Moves a job one position closer to the head of the "user" queue (the job position in the queue is swapped with
     * the one of the job of the same user that is closer to the head.
     * If the job is already at the head of the queue, the method does nothing.
     *
     * @param user  The user
     * @param jobId The job identifier
     */
    public void moveJobToHead(String user, long jobId) {
        synchronized (lock) {
            if (!this.queue.isEmpty() && !this.queue.peek().getKeyOne().equals(jobId)) {
                final Stack<Tuple<Long, String>> stack = new Stack<>();
                Tuple<Long, String> swapped = null;
                final Iterator<Tuple<Long, String>> iterator = ((ArrayDeque<Tuple<Long, String>>) this.queue).descendingIterator();
                while (iterator.hasNext()) {
                    final Tuple<Long, String> pair = iterator.next();
                    if (!pair.getKeyTwo().equals(user)) {
                        stack.push(pair);
                    } else {
                        if (pair.getKeyOne() != jobId) {
                            if (swapped == null) {
                                stack.push(pair);
                            } else {
                                stack.push(pair);
                                stack.push(swapped);
                                swapped = null;
                            }
                        } else {
                            swapped = pair;
                        }
                    }
                    iterator.remove();
                }
                while (!stack.isEmpty()) {
                    this.queue.offer(stack.pop());
                }
            }
        }
    }
    /**
     * Moves a job one position closer to the tail of the queue, regardless the user.
     * If the job is already at the tail of the queue, the method does nothing.
     *
     * @param jobId The job identifier
     */
    public void moveJobToTail(long jobId) {
        synchronized (lock) {
            final ArrayDeque<Tuple<Long, String>> deque = (ArrayDeque<Tuple<Long, String>>) this.queue;
            if (!deque.getLast().getKeyOne().equals(jobId)) {
                final Iterator<Tuple<Long, String>> iterator = this.queue.iterator();
                final Queue<Tuple<Long, String>> newQueue = new ArrayDeque<>();
                Tuple<Long, String> swapped = null;
                while (iterator.hasNext()) {
                    final Tuple<Long, String> pair = iterator.next();
                    if (!pair.getKeyOne().equals(jobId)) {
                        newQueue.offer(pair);
                        if (swapped != null) {
                            newQueue.offer(swapped);
                            swapped = null;
                        }
                    } else {
                        swapped = pair;
                    }
                    iterator.remove();
                }
                this.queue.addAll(newQueue);
            }
        }
    }
    /**
     * Moves a job one position closer to the tail of the "user" queue (the job position in the queue is swapped with
     * the one of the job of the same user that is closer to the tail.
     * If the job is already at the tail of the queue, the method does nothing.
     *
     * @param user  The user
     * @param jobId The job identifier
     */
    public void moveJobToTail(String user, long jobId) {
        synchronized (lock) {
            final ArrayDeque<Tuple<Long, String>> deque = (ArrayDeque<Tuple<Long, String>>) this.queue;
            if (!deque.getLast().getKeyOne().equals(jobId)) {
                final Iterator<Tuple<Long, String>> iterator = deque.descendingIterator();
                final Stack<Tuple<Long, String>> stack = new Stack<>();
                Tuple<Long, String> swapped = null;
                while (iterator.hasNext()) {
                    final Tuple<Long, String> pair = iterator.next();
                    if (!pair.getKeyOne().equals(jobId)) {
                        if (swapped == null) {
                            stack.push(pair);
                        } else {
                            stack.push(swapped);
                            stack.push(pair);
                            swapped = null;
                        }
                    } else {
                        swapped = pair;
                    }
                    iterator.remove();
                }
                while (!stack.isEmpty()) {
                    this.queue.offer(stack.pop());
                }
            }
        }
    }

    /**
     * Lists all the queued jobs of a user
     * @param user  The name of the user
     */
    public List<Long> getUserJobs(String user) {
        synchronized (lock) {
            return this.queue.stream().map(Tuple::getKeyOne).collect(Collectors.toList());
        }
    }

    /**
     * Lists all the jobs for all the users, grouped by user
     */
    public Map<String, List<Long>> getUserQueues() {
        return this.queue.stream()
                         .collect(Collectors.groupingBy(Tuple::getKeyTwo,
                                                        Collectors.collectingAndThen(Collectors.toList(),
                                                                                     list -> list.stream().map(Tuple::getKeyOne)
                                                                                                 .collect(Collectors.toList()))));
    }

    /**
     * Lists the contents of the job queue.
     */
    public Queue<Tuple<Long, String>> getAllJobs() {
        return new ArrayDeque<>(this.queue);
    }

    /**
     * Retrieves the next job for a particular user.
     * @param user  The name of the user
     */
    public ExecutionJob take(String user) {
        synchronized (lock) {
            ExecutionJob job = null;
            final Iterator<Tuple<Long, String>> iterator = this.queue.iterator();
            while (iterator.hasNext()) {
                final Tuple<Long, String> next = iterator.next();
                if (next.getKeyTwo().equals(user)) {
                    iterator.remove();
                    job = this.jobProvider.get(next.getKeyOne());
                    break;
                }
            }
            return job;
        }
    }

    /**
     * Retrieves the job that on the head of the queue.
     */
    public ExecutionJob take() {
        synchronized (lock) {
            ExecutionJob job = null;
            final Tuple<Long, String> pair = this.jobSelector.chooseNext();
            if (pair != null) {
                job = this.jobProvider.get(pair.getKeyOne());
            }
            return job;
        }
    }

    /**
     * Checks if there queue contains more jobs for a given user.
     *
     * @param user  The user
     */
    public boolean hasMoreJobs(String user) {
        synchronized (lock) {
            return this.queue.stream().anyMatch(p -> p.getKeyTwo().equals(user));
        }
    }
}
