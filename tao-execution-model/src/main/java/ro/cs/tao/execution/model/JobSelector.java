package ro.cs.tao.execution.model;

import java.util.Queue;

/**
 * Generic implementation of a job-choosing from the job queue.
 * Extenders may override it so that a different algorithm is used.
 *
 * @author Cosmin Cara
 */
public class JobSelector<T> {
    private final Queue<T> queue;

    public JobSelector(Queue<T> queue) {
        this.queue = queue;
    }

    /**
     * Returns the next element, or <code>null</code> if no element is queued.
     */
    public T chooseNext() {
        return this.queue.poll();
    }

    protected Queue<T> getQueue() { return this.queue; }
}
