package ro.cs.tao.orchestration.queue;

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.utils.Tuple;

import java.util.List;
import java.util.Map;
import java.util.Queue;

public interface JobQueue {
    /**
     * Initializes the queue with the jobs that have not yet been executed.
     */
    void initialize();

    /**
     * Adds a job to the user queue and returns its position in the queue.
     *
     * @param job The job to be queued
     */
    int put(ExecutionJob job);

    /**
     * Removes a job from the queue, regardless its position in the queue.
     *
     * @param jobId The job identifier
     */
    boolean removeJob(long jobId);

    /**
     * Removes a job from the queue, regardless its position in the queue.
     *
     * @param userId  The user identifier
     * @param jobId The job identifier
     */
    default boolean removeJob(String userId, long jobId) {
        return removeJob(jobId);
    }

    /**
     * Removes all the queued jobs for a user.
     * If no user is specified, all the queued jobs are removed.
     *
     * @param userId The user identifier
     */
    List<Long> removeUserJobs(String userId);

    /**
     * Moves a job one position closer to the head of the queue, regardless the user.
     * If the job is already at the head of the queue, the method does nothing.
     *
     * @param jobId The job identifier
     */
    void moveJobToHead(long jobId);

    /**
     * Moves a job one position closer to the head of the "user" queue (the job position in the queue is swapped with
     * the one of the job of the same user that is closer to the head.
     * If the job is already at the head of the queue, the method does nothing.
     *
     * @param userId The user
     * @param jobId  The job identifier
     */
    default void moveJobToHead(String userId, long jobId) {
        moveJobToHead(jobId);
    }

    /**
     * Moves a job one position closer to the tail of the queue, regardless the user.
     * If the job is already at the tail of the queue, the method does nothing.
     *
     * @param jobId The job identifier
     */
    void moveJobToTail(long jobId);

    /**
     * Moves a job one position closer to the tail of the "user" queue (the job position in the queue is swapped with
     * the one of the job of the same user that is closer to the tail.
     * If the job is already at the tail of the queue, the method does nothing.
     *
     * @param userId  The user identifier
     * @param jobId The job identifier
     */
    default void moveJobToTail(String userId, long jobId) {
        moveJobToTail(jobId);
    }

    /**
     * Lists all the queued jobs of a user
     *
     * @param userId The user identifier
     */
    List<Long> getUserJobs(String userId);

    /**
     * Lists all the jobs for all the users, grouped by user
     */
    Map<String, List<Long>> getUserQueues();

    /**
     * Lists the contents of the job queue.
     */
    Queue<Tuple<Long, String>> getAllJobs();

    /**
     * Retrieves the job that on the head of the queue.
     */
    ExecutionJob take();

    /**
     * Retrieves the next job for a particular user.
     *
     * @param userId The user identifier
     */
    default ExecutionJob take(String userId) {
        return take();
    }

    /**
     * Retrieves the next job for a user different than the argument
     *
     * @param userId The user identifier to be excluded
     */
    default ExecutionJob takeExcept(String userId) {
        return take();
    }

    /**
     * Checks if the queue contains more jobs for a given user.
     *
     * @param userId The user identifier
     */
    boolean hasMoreJobs(String userId);
}
