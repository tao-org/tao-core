package ro.cs.tao.utils;

import java.util.*;
import java.util.function.Function;

/**
 * A simple cache implementation that clears its entries if not accessed in a given time interval.
 *
 * @param <T>   The key type
 * @param <U>   The value type
 *
 * @author  Cosmin Cara
 * @since   1.4
 */
public class AutoEvictableCache<T, U> {
    private final Map<T, Tuple<U, Long>> cache;
    private final Function<T, U> functor;

    /**
     * Constructs a cache instance with the given retention period.
     *
     * @param functor               The function that produces values given keys.
     * @param retentionInSeconds    The retention period in seconds
     */
    public AutoEvictableCache(Function<T, U> functor, long retentionInSeconds) {
        this.cache = Collections.synchronizedMap(new HashMap<>());
        this.functor = functor;
        long retention = retentionInSeconds * 1000;
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long current = System.currentTimeMillis();
                AutoEvictableCache.this.cache.entrySet().removeIf(e -> e.getValue().getKeyTwo() - current >= retention);
            }
        }, retention, retention);
    }
    /**
     * Returns the value associated with the key (if any) and updates the accessed timestamp.
     * @param key   The key to lookup
     */
    public U get(T key) {
        Tuple<U, Long> tuple = this.cache.get(key);
        U value;
        if (tuple != null) {
            value = tuple.getKeyOne();
            this.cache.replace(key, new Tuple<>(value, System.currentTimeMillis()));
        } else {
            value = functor.apply(key);
            this.cache.put(key, new Tuple<>(value, System.currentTimeMillis()));
        }
        return value;
    }
    /**
     * Removes the value associated with the key, or null if there is no such key
     * @param key   The key to lookup
     */
    public U remove(T key) {
        Tuple<U, Long> tuple = this.cache.remove(key);
        return tuple != null ? tuple.getKeyOne() : null;
    }
    /**
     * Returns the number of cache entries
     */
    public int size() {
        return this.cache.size();
    }

    /**
     * Clears the cache
     */
    public void clear() {
        this.cache.clear();
    }
}
