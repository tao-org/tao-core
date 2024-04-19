package ro.cs.tao.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Base class for file-backed collections (collections that persist their state to disk).
 *
 * @param <E>   The type of the collection elements
 * @param <V>   The type of the collection
 *
 * @author  Cosmin Cara
 * @since   1.4.3
 */
public abstract class FileBackedCollection<E, V extends Collection<E>> implements Collection<E> {
    protected final Path file;
    protected final V collection;
    protected final Class<E> elementClass;

    public FileBackedCollection(Path file, Class<E> elementClass) {
        if (file == null) {
            throw new NullPointerException("file");
        }
        this.file = file;
        this.elementClass = elementClass;
        this.collection = newCollection();
        try {
            Files.createDirectories(this.file.getParent());
            if (Files.exists(this.file) && Files.size(this.file) > 2) {
                final List<E> items = readItems();
                if (items != null) {
                    this.collection.addAll(items);
                }
            } else if (Files.notExists(this.file)){
                Files.createFile(this.file);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return collection.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return collection.iterator();
    }

    @Override
    public Object[] toArray() {
        return collection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return collection.toArray(a);
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return collection.toArray(generator);
    }

    @Override
    public boolean add(E e) {
        return wrapBoolean(collection.add(e));
    }

    @Override
    public boolean remove(Object o) {
        return wrapBoolean(collection.remove(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return collection.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return wrapBoolean(collection.addAll(c));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return wrapBoolean(collection.removeAll(c));
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return wrapBoolean(collection.removeIf(filter));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return wrapBoolean(collection.retainAll(c));
    }

    @Override
    public void clear() {
        collection.clear();
        flush();
    }

    @Override
    public boolean equals(Object o) {
        return collection.equals(o);
    }

    @Override
    public int hashCode() {
        return collection.hashCode();
    }

    @Override
    public Spliterator<E> spliterator() {
        return collection.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return collection.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return collection.parallelStream();
    }

    /**
     * Creates an instance of the inner collection
     */
    protected abstract V newCollection();

    protected List<E> readItems() throws IOException {
        final ObjectReader reader = new ObjectMapper().readerFor(new TypeReference<List<E>>() {});
        return reader.readValue(this.file.toFile());
    }

    /**
     * Helper method to wrap actual collection methods and flush the collection to disk
     * only if the collection operation was successful.
     *
     * @param value The result of the collection method
     */
    protected boolean wrapBoolean(boolean value) {
        if (value) {
            flush();
        }
        return value;
    }

    /**
     * Helper method to wrap actual collection methods and flush the collection to disk
     * only if the collection operation was successful.
     *
     * @param element The result of the collection method
     */
    protected E wrapElement(E element) {
        if (element != null) {
            flush();
        }
        return element;
    }

    protected void flush() {
        try {
            Files.write(this.file, new ObjectMapper().writerFor(collection.getClass())
                                                     .writeValueAsBytes(this.collection), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
