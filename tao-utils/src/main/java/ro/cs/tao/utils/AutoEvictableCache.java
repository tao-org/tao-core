package ro.cs.tao.utils;

import java.util.*;
import java.util.function.Function;

public class AutoEvictableCache<T, U> {
    private final Map<T, Tuple<U, Long>> cache;
    private final Function<T, U> functor;

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

    public int size() {
        return this.cache.size();
    }

    public void clear() {
        this.cache.clear();
    }
}
