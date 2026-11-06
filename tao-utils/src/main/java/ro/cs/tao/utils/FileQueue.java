package ro.cs.tao.utils;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * FileQueue is a file-backed implementation of the Queue interface that persists its elements on disk.
 * It extends FileBackedCollection to provide the functionality of a queue with on-disk persistence.
 *
 * @param <E> The type of elements stored in this queue
 */
public class FileQueue<E> extends FileBackedCollection<E, Queue<E>> implements Queue<E> {

    public FileQueue(Path file, Class<E> elementClass) {
        super(file, elementClass);
    }

    @Override
    public boolean offer(E e) {
        return wrapBoolean(this.collection.offer(e));
    }

    @Override
    public E remove() {
        return wrapElement(this.collection.remove());
    }

    @Override
    public E poll() {
        return wrapElement(this.collection.poll());
    }

    @Override
    public E element() {
        return this.collection.element();
    }

    @Override
    public E peek() {
        return this.collection.peek();
    }

    public Iterator<E> descendingIterator() {
        return ((ConcurrentLinkedDeque<E>) this.collection).descendingIterator();
    }

    public E getLast() {
        return ((ConcurrentLinkedDeque<E>) this.collection).getLast();
    }

    @Override
    protected ConcurrentLinkedDeque<E> newCollection() {
        return new ConcurrentLinkedDeque<>();
    }
}
