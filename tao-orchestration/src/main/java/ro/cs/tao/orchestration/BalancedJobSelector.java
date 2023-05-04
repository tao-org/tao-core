package ro.cs.tao.orchestration;

import ro.cs.tao.execution.model.JobSelector;
import ro.cs.tao.utils.Tuple;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Job selection strategy that picks the next job from queue trying to keep a balanced number of jobs per user.
 *
 * @author  Cosmin Cara
 * @since   1.4.3
 */
public class BalancedJobSelector extends JobSelector<Tuple<Long, String>> {

    public BalancedJobSelector(Queue<Tuple<Long, String>> queue) {
        super(queue);
    }

    @Override
    public Tuple<Long, String> chooseNext() {
        final Queue<Tuple<Long, String>> queue = getQueue();
        if (queue.isEmpty()) {
            return null;
        }
        final Map<String, Integer> counts = new HashMap<>();
        final Map<String, Tuple<Long, String>> firstOccurrences = new HashMap<>();
        for (Tuple<Long, String> current : queue) {
            counts.compute(current.getKeyTwo(), (key, value) -> value != null ? value + 1 : 1);
            if (!firstOccurrences.containsKey(current.getKeyTwo())) {
                firstOccurrences.put(current.getKeyTwo(), current);
            }
        }
        final Map.Entry<String, Integer> maxEntry = counts.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).get();
        return firstOccurrences.get(maxEntry.getKey());
    }
}
