package ro.cs.tao.utils;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;

public class FileQueue<E> extends FileBackedCollection<E, Queue<E>> implements Queue<E> {

    public FileQueue(Path file) {
        super(file);
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

    @Override
    protected ArrayDeque<E> newCollection() {
        return new ArrayDeque<>();
    }
}
