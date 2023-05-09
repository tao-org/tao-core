package ro.cs.tao.orchestration;

import ro.cs.tao.execution.model.JobSelector;
import ro.cs.tao.utils.Tuple;

import java.util.Queue;

public class DefaultJobSelector extends JobSelector<Tuple<Long, String>> {

    public DefaultJobSelector(Queue<Tuple<Long, String>> queue) {
        super(queue);
    }
}
