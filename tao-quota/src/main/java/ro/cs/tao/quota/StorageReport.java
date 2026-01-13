package ro.cs.tao.quota;

import ro.cs.tao.utils.Tuple;
import ro.cs.tao.utils.executors.MemoryUnit;

import java.util.*;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.stream.Collectors;

/**
 * The StorageReport class is responsible for maintaining and managing
 * storage information for a specific repository type. It tracks the
 * storage usage of various users, allowing for the aggregation and
 * retrieval of total storage in a specified memory unit.
 *
 * The class relies on the RepositoryType enumeration to categorize
 * the repository to which the storage details belong. Storage data
 * is maintained in a map structure where each user's storage usage
 * is recorded along with the unit of measurement.
 */
public class StorageReport {
        private Map<String, Tuple<Double, MemoryUnit>> userStorage;

    public StorageReport() {
    }

    /**
     * Retrieves the user storage information tracked by this storage report.
     * The information is represented as a map where the keys are user identifiers (String),
     * and the values are tuples containing the storage amount (Double) and the corresponding
     * memory unit (MemoryUnit).
     *
     * @return a map of user storage data, with each entry consisting of a user ID as the key
     *         and a tuple of storage amount and memory unit as the value.
     */
    public Map<String, Tuple<Double, MemoryUnit>> getUserStorage() {
        if (userStorage == null) {
            userStorage = new HashMap<>();
        }
        return userStorage;
    }

    /**
     * Sorts and retrieves the user storage information in descending order
     * based on the storage size in bytes. The storage size for each user is
     * calculated by multiplying the storage amount by the corresponding memory
     * unit's value in bytes.
     *
     * @return a map of user storage data, sorted in descending order of storage size.
     *         Each entry in the map consists of a user ID as the key and a tuple
     *         containing the storage amount and memory unit as the value.
     */
    public Map<String, Tuple<Double, MemoryUnit>> getUserStorageBySizeDescending() {
        Map<String, Tuple<Double, MemoryUnit>> storage = getUserStorage();
        return storage.entrySet().stream().sorted(Map.Entry.comparingByValue(new Comparator<Tuple<Double, MemoryUnit>>() {
            @Override
            public int compare(Tuple<Double, MemoryUnit> o1, Tuple<Double, MemoryUnit> o2) {
                return Double.compare(o2.getKeyOne() * o2.getKeyTwo().value(),
                                      o1.getKeyOne() * o1.getKeyTwo().value());
            }
        })).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> v, LinkedHashMap::new));
    }

    /**
     * Filters and retrieves the user storage information based on a specified minimum
     * storage size in bytes, and sorts the results in descending order by the storage size.
     * The minimum storage size is calculated using the provided threshold and memory unit,
     * and the storage size for each user is determined by multiplying the storage amount
     * with the corresponding memory unit's value in bytes.
     *
     * @param lowThreshold the minimum storage size threshold; storage sizes lower than this value
     *                     will be excluded from the result, expressed as a {@code double}.
     * @param unit         the memory unit used to specify the storage size threshold,
     *                     represented as a {@link MemoryUnit}.
     * @return a map of filtered user storage data, sorted in descending order by storage size.
     *         Each entry in the map contains a user ID as the key and a tuple of storage
     *         amount and memory unit as the value.
     */
    public Map<String, Tuple<Double, MemoryUnit>> getUserStorageBySizeDescending(double lowThreshold, MemoryUnit unit) {
        Map<String, Tuple<Double, MemoryUnit>> storage = getUserStorage();
        return storage.entrySet().stream()
                      .filter(e -> e.getValue().getKeyOne() * e.getValue().getKeyTwo().value() >= lowThreshold * unit.value())
                      .sorted(Map.Entry.comparingByValue(new Comparator<Tuple<Double, MemoryUnit>>() {
            @Override
            public int compare(Tuple<Double, MemoryUnit> o1, Tuple<Double, MemoryUnit> o2) {
                return Double.compare(o2.getKeyOne() * o2.getKeyTwo().value(),
                                      o1.getKeyOne() * o1.getKeyTwo().value());
            }
        })).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> v, LinkedHashMap::new));
    }

    /**
     * Retrieves the user storage information sorted by user names in ascending order.
     * The storage information is represented as a map where the keys are user identifiers (String),
     * and the values are tuples containing the storage amount (Double) and the corresponding memory unit (MemoryUnit).
     *
     * @return a map of user storage data, sorted by user names in ascending order.
     *         Each entry consists of a user ID as the key and a tuple of storage amount
     *         and memory unit as the value.
     */
    public Map<String, Tuple<Double, MemoryUnit>> getUserStorageByName() {
        return new TreeMap<>(getUserStorage());
    }

    public void setUserStorage(Map<String, Tuple<Double, MemoryUnit>> userStorage) {
        this.userStorage = userStorage;
    }

    /**
     * Adds or updates the storage information for a specific user in the storage report.
     * If the storage map is not initialized, a new map is created to store the data.
     * Each user's storage is stored as a tuple containing the storage amount and its unit.
     *
     * @param userId the unique identifier of the user whose storage information is being added or updated
     * @param storage the amount of storage to be associated with the user, represented as a {@link Double}
     * @param unit the unit of storage, represented as a {@link MemoryUnit}
     */
    public void addUserStorage(String userId, Double storage,  MemoryUnit unit) {
        if (this.userStorage == null) {
            this.userStorage = new HashMap<String, Tuple<Double, MemoryUnit>>();
        }
        this.userStorage.put(userId, new Tuple(storage, unit));
    }

    /**
     * Calculates the total storage across all users in the specified memory unit.
     * The total is computed by iterating over all user storage entries in the storage map,
     * converting each storage amount to the specified unit, and summing the results.
     *
     * @param unit the memory unit to which all storage amounts should be converted,
     *             represented as a {@link MemoryUnit}.
     * @return the total storage, expressed as a {@code double} value in the specified memory unit.
     */
    public double getTotal(MemoryUnit unit) {
        if (this.userStorage == null || this.userStorage.isEmpty()) {
            return 0;
        }
        final DoubleAccumulator total = new DoubleAccumulator(Double::sum, 0);
        this.userStorage.values().forEach(tuple -> { total.accumulate(tuple.getKeyOne() * tuple.getKeyTwo().value() / unit.value());});
        return total.doubleValue();
    }
}
