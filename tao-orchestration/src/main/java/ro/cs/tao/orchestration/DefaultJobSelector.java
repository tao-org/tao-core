package ro.cs.tao.orchestration;

import ro.cs.tao.execution.model.JobSelector;
import ro.cs.tao.utils.Tuple;

import java.util.Queue;

/**
 * Default implementation of the {@link JobSelector} class that operates on
 * a queue of {@link Tuple} objects where the first element is of type {@link Long}
 * and the second element is of type {@link String}.
 *
 * This class leverages the default behavior of the {@link JobSelector},
 * which selects and removes the next element from the underlying queue
 * using the FIFO algorithm, unless further overridden.
 *
 * The generic type {@link Tuple} allows the implementation to handle jobs
 * represented as a pair of values: a {@link Long} as the primary identifier and
 * a {@link String} as the secondary descriptor.
 *
 * The constructor allows initializing the selector with a specific queue from
 * which jobs will be selected.
 */
public class DefaultJobSelector extends JobSelector<Tuple<Long, String>> {

    public DefaultJobSelector(Queue<Tuple<Long, String>> queue) {
        super(queue);
    }
}
