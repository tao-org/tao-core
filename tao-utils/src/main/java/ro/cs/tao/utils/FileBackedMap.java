package ro.cs.tao.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A map that is backed by a file.
 * The map persists its state on any modification and can restore itself from a file.
 * The keys and the values of the map are serialized as Json.
 *
 * @param <K>   The key type
 * @param <V>   The value type
 *
 * @author Cosmin Cara
 * @since  1.4.1
 */
public class FileBackedMap<K, V>  implements Map<K, V> {
    private final Path file;
    private final Map<K, V> map;
    private final ObjectWriter writer;

    /**
     * Constructs a new file-backed map.
     * If the given file already contains map state, the map will be re-populated.
     *
     * @param file  The backing file
     */
    public FileBackedMap(Path file) {
        if (file == null) {
            throw new NullPointerException("file");
        }
        this.file = file;
        this.map = new LinkedHashMap<>();
        try {
            FileUtilities.createDirectories(this.file.getParent());
            if (Files.exists(this.file) && Files.size(this.file) > 2) {
                final ObjectReader reader = new ObjectMapper().readerFor(LinkedHashMap.class);
                this.map.putAll(reader.readValue(this.file.toFile()));
            } else if (Files.notExists(this.file)){
                Files.createFile(this.file);
            }
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            final SimpleModule module = new SimpleModule();
            module.addDeserializer(Tuple.class, new JsonDeserializer<Tuple>() {
                @Override
                public Tuple deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
                    JsonNode node = jsonParser.readValueAsTree();
                    try (JsonParser parser = mapper.createParser(node.toString())) {
                        final ObjectCodec codec = parser.getCodec();
                        JsonNode innerNode = parser.readValueAsTree();
                        return codec.treeToValue(innerNode, Tuple.class);
                    }
                }
            });
            mapper.registerModule(module);
            this.writer = mapper.writer();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        V retVal = this.map.put(key, value);
        flush();
        return retVal;
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
            Files.write(this.file, this.writer.writeValueAsBytes(this.map), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
