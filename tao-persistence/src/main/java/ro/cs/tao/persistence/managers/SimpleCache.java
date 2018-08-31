/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package ro.cs.tao.persistence.managers;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Simple cache for entities.
 *
 * @author Cosmin Cara
 */
public class SimpleCache {
    private static final Map<Class, Cache> cacheMap;

    static {
        cacheMap = new HashMap<>();
    }

    public static <K, V> Cache<K, V> create(Class<K> keyClass, Class<V> valueClass,
                                            Function<K, V> loader) {
        Cache<K, V> cache = (Cache<K, V>) cacheMap.get(valueClass);
        if (cache == null) {
            cache = new Cache<>(100, loader);
            cacheMap.put(valueClass, cache);
        }
        return cache;
    }

    public static <K, V> Cache<K, V> getCache(Class<V> valueClass) {
        return (Cache<K, V>) cacheMap.get(valueClass);
    }

    public static class Cache<K, V> {
        private final LinkedHashMap<K, V> cacheMap;
        private final int limit;
        private final Function<K, V> loader;

        private Cache(int maxEntries, Function<K, V> loader) {
            this.limit = maxEntries;
            this.loader = loader;
            this.cacheMap = new LinkedHashMap<K, V>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    return size() > limit;
                }
            };
        }

        public V get(K key) {
            synchronized (cacheMap) {
                return cacheMap.computeIfAbsent(key, loader);
            }
        }

        public void put(K key, V value) {
            synchronized (cacheMap) {
                cacheMap.put(key, value);
            }
        }

        public void remove(K key) {
            synchronized (cacheMap) {
                cacheMap.remove(key);
            }
        }
    }
}
