package ro.cs.tao.utils;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

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
        return ((ArrayDeque<E>) this.collection).descendingIterator();
    }

    public E getLast() {
        return ((ArrayDeque<E>) this.collection).getLast();
    }

    @Override
    protected ArrayDeque<E> newCollection() {
        return new ArrayDeque<>();
    }
}
