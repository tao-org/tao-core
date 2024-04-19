package ro.cs.tao.orchestration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import ro.cs.tao.component.SystemVariable;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.JobSelector;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.utils.FileQueue;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.utils.Tuple;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Simple implementation of a job queue backed by a file.
 *
 * @author Cosmin Cara
 * @since 1.4.3
 */
public class DefaultJobQueue implements ro.cs.tao.orchestration.queue.JobQueue {
    private final FileQueue<Tuple<Long, String>> queue;
    private final ExecutionJobProvider jobProvider;
    private JobSelector<Tuple<Long, String>> jobSelector;
    private volatile boolean notEmpty;
    private final Logger logger = Logger.getLogger(DefaultJobQueue.class.getName());

    public DefaultJobQueue(ExecutionJobProvider jobProvider) {
        Tuple<Long, String> dummy = new Tuple<>(0L, "");
        this.queue = new FileQueue<>(Paths.get(SystemVariable.CACHE.value()).resolve("wait_jobs.json"),
                                     (Class<Tuple<Long, String>>) dummy.getClass()) {
            @Override
            protected List<Tuple<Long, String>> readItems() throws IOException {
                final ObjectMapper mapper = new ObjectMapper();
                final CollectionType type = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Tuple.class);
                final List<Tuple<? extends Number, String>> list = mapper.readValue(this.file.toFile(), type);
                final List<Tuple<Long, String>> casted = new ArrayList<>();
                for (Tuple<? extends Number, String> tuple : list) {
                    casted.add(new Tuple<>(tuple.getKeyOne().longValue(), tuple.getKeyTwo()));
                }
                return casted;
            }
        };
        this.jobProvider = jobProvider;
        String jobSelectorClass = ConfigurationManager.getInstance().getValue("job.selector");
        try {
            if (!StringUtilities.isNullOrEmpty(jobSelectorClass)) {
                final Class<?> clazz = Class.forName(jobSelectorClass);
                if (JobSelector.class.isAssignableFrom(clazz)) {
                    this.jobSelector = (JobSelector<Tuple<Long, String>>) clazz.getConstructor(Queue.class).newInstance(this.queue);
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
    @Override
    public void initialize() {
        final Set<ExecutionStatus> statuses = new HashSet<>() {{
            add(ExecutionStatus.QUEUED_ACTIVE);
            add(ExecutionStatus.RUNNING);
        }};
        final List<ExecutionJob> runnableJobs = this.jobProvider.list(statuses);
        if (runnableJobs != null) {
            for (ExecutionJob job : runnableJobs) {
                job.setExecutionStatus(ExecutionStatus.UNDETERMINED);
                job.setStartTime(null);
                job.setEndTime(null);
                try {
                    this.jobProvider.update(job);
                    logger.fine(String.format("Job [%s] was reset to UNDETERMINED", job.getName()));
                } catch (PersistenceException e) {
                    logger.warning(String.format("Error resetting job [%s]. Reason: %s", job.getName(), e.getMessage()));
                }
                put(job);
            }
        }
    }

    /**
     * Adds a job to the user queue and returns its position in the queue.
     * @param job   The job to be queued
     */
    @Override
    public synchronized int put(ExecutionJob job) {
        this.queue.offer(new Tuple<>(job.getId(), job.getUserId()));
        logger.finest(String.format("Job [%s] for user '%s' was added to the queue with status %s",
                                    job.getName(), job.getUserId(), job.getExecutionStatus().name()));
        notEmpty = true;
        return this.queue.size();
    }

    /**
     * Removes a job from the queue, regardless its position in the queue.
     *
     * @param job  The job object
     */
    @Override
    public synchronized boolean removeJob(long jobId) {
        return this.queue.removeIf(p -> p.getKeyOne().equals(jobId));
    }

    /**
     * Removes a job from the queue, regardless its position in the queue.
     *
     * @param userId  The user
     * @param jobId The job identifier
     */
    @Override
    public synchronized boolean removeJob(String userId, long jobId) {
        return queue.removeIf(p -> p.getKeyOne().equals(jobId) && p.getKeyTwo().equals(userId));
    }

    /**
     * Removes all the queued jobs for a user.
     * If no user is specified, all the queued jobs are removed.
     *
     * @param userId  The name of the user
     */
    @Override
    public synchronized List<Long> removeUserJobs(String userId) {
        List<Long> removedJobs = null;
        final Iterator<Tuple<Long, String>> iterator = this.queue.iterator();
        while (iterator.hasNext()) {
            final Tuple<Long, String> next = iterator.next();
            if (userId == null || next.getKeyTwo().equals(userId)) {
                iterator.remove();
                if (removedJobs == null) {
                    removedJobs = new ArrayList<>();
                }
                removedJobs.add(next.getKeyOne());
            }
        }
        return removedJobs;
    }

    /**
     * Moves a job one position closer to the head of the queue, regardless the user.
     * If the job is already at the head of the queue, the method does nothing.
     *
     * @param jobId The job identifier
     */
    @Override
    public synchronized void moveJobToHead(long jobId) {
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
    /**
     * Moves a job one position closer to the head of the "user" queue (the job position in the queue is swapped with
     * the one of the job of the same user that is closer to the head.
     * If the job is already at the head of the queue, the method does nothing.
     *
     * @param userId  The user
     * @param jobId The job identifier
     */
    @Override
    public synchronized void moveJobToHead(String userId, long jobId) {
        if (!this.queue.isEmpty() && !this.queue.peek().getKeyOne().equals(jobId)) {
            final Stack<Tuple<Long, String>> stack = new Stack<>();
            Tuple<Long, String> swapped = null;
            final Iterator<Tuple<Long, String>> iterator = this.queue.descendingIterator();
            while (iterator.hasNext()) {
                final Tuple<Long, String> pair = iterator.next();
                if (!pair.getKeyTwo().equals(userId)) {
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
    /**
     * Moves a job one position closer to the tail of the queue, regardless the user.
     * If the job is already at the tail of the queue, the method does nothing.
     *
     * @param jobId The job identifier
     */
    @Override
    public synchronized void moveJobToTail(long jobId) {
        if (!this.queue.getLast().getKeyOne().equals(jobId)) {
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
    /**
     * Moves a job one position closer to the tail of the "user" queue (the job position in the queue is swapped with
     * the one of the job of the same user that is closer to the tail.
     * If the job is already at the tail of the queue, the method does nothing.
     *
     * @param userId  The user
     * @param jobId The job identifier
     */
    @Override
    public synchronized void moveJobToTail(String userId, long jobId) {
        if (!this.queue.getLast().getKeyOne().equals(jobId)) {
            final Iterator<Tuple<Long, String>> iterator = this.queue.descendingIterator();
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

    /**
     * Lists all the queued jobs of a user
     * @param userId  The user identifier
     */
    @Override
    public List<Long> getUserJobs(String userId) {
        return this.queue.stream().filter(t -> t.getKeyTwo().equals(userId)).map(Tuple::getKeyOne).collect(Collectors.toList());
    }

    /**
     * Lists all the jobs for all the users, grouped by user
     */
    @Override
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
    @Override
    public Queue<Tuple<Long, String>> getAllJobs() {
        return new ArrayDeque<>(this.queue);
    }

    /**
     * Retrieves the next job for a particular user.
     * @param userId  The user identifier
     */
    @Override
    public ExecutionJob take(String userId) {
        if (userId == null) {
            return take();
        }
        ExecutionJob job = null;
        while (this.queue.isEmpty()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        synchronized (this.queue) {
            final Iterator<Tuple<Long, String>> iterator = this.queue.iterator();
            while (iterator.hasNext()) {
                final Tuple<Long, String> next = iterator.next();
                if (next.getKeyTwo().equals(userId)) {
                    iterator.remove();
                    job = this.jobProvider.get(next.getKeyOne());
                    break;
                }
            }
        }
        return job;
    }

    @Override
    public ExecutionJob takeExcept(String userId) {
        if (userId == null) {
            return take();
        }
        ExecutionJob job = null;
        while (this.queue.isEmpty()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        synchronized (this.queue) {
            final Iterator<Tuple<Long, String>> iterator = this.queue.iterator();
            while (iterator.hasNext()) {
                final Tuple<Long, String> next = iterator.next();
                if (!next.getKeyTwo().equals(userId)) {
                    iterator.remove();
                    job = this.jobProvider.get(next.getKeyOne());
                    break;
                }
            }
        }
        // All the queued jobs are of the same user, return the first one
        if (job == null) {
            return take();
        }
        return job;
    }

    /**
     * Retrieves the job that on the head of the queue.
     */
    @Override
    public ExecutionJob take() {
        ExecutionJob job = null;
        while (this.queue.isEmpty()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        synchronized (this.queue) {
            final Tuple<Long, String> pair = this.jobSelector.chooseNext();
            if (pair != null) {
                job = this.jobProvider.get(pair.getKeyOne());
            }
        }
        return job;
    }

    /**
     * Checks if the queue contains more jobs for a given user.
     *
     * @param userId  The user identifier
     */
    @Override
    public boolean hasMoreJobs(String userId) {
        return this.queue.stream().anyMatch(p -> p.getKeyTwo().equals(userId));
    }
}
