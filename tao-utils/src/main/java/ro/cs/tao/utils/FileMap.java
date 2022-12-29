package ro.cs.tao.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FileMap<K, V> implements Map<K, V> {
    private final Path file;
    private final Map<K, V> map;

    public FileMap(Path file) {
        this.file = file;
        this.map = new LinkedHashMap<>();
        try {
            if (Files.exists(this.file) && Files.size(this.file) > 0) {
                this.map.putAll(new ObjectMapper().reader().readValue(this.file.toFile()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.map.get(key);
    }

    @Override
    public V put(K key, V value) {
        return this.map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        final V result = this.map.remove(key);
        flush();
        return result;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> other) {
        this.map.putAll(other);
        flush();
    }

    @Override
    public void clear() {
        this.map.clear();
        try {
            Files.deleteIfExists(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.map.entrySet();
    }

    private void flush() {
        try {
            Files.write(this.file, new ObjectMapper().writer().writeValueAsBytes(this.map), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
