package ro.cs.tao.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Specialization of a blocking queue that adds hashing to its elements, so that
 * the check for existence of an element has O(1) complexity.
 *
 * @param <E>   The type of elements
 * @param <I>   The type of index
 *
 * @author  Cosmin Cara
 * @since   1.4
 */
public class HashedBlockingQueue<E, I> extends LinkedBlockingQueue<E> {
    private final Set<I> index;
    private final Function<E, I> indexFunction;

    public HashedBlockingQueue(Function<E, I> indexFunction) {
        this.index = new HashSet<>();
        this.indexFunction = indexFunction;
    }

    public HashedBlockingQueue(int capacity, Function<E, I> indexFunction) {
        super(capacity);
        this.index = new HashSet<>();
        this.indexFunction = indexFunction;
    }

    public HashedBlockingQueue(Collection<? extends E> c, Function<E, I> indexFunction) {
        super(c);
        this.index = new HashSet<>();
        this.indexFunction = indexFunction;
    }

    @Override
    public void put(E e) throws InterruptedException {
        this.index.add(this.indexFunction.apply(e));
        super.put(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        this.index.add(this.indexFunction.apply(e));
        return super.offer(e, timeout, unit);
    }

    @Override
    public boolean offer(E e) {
        this.index.add(this.indexFunction.apply(e));
        return super.offer(e);
    }

    @Override
    public E take() throws InterruptedException {
        E item = super.take();
        this.index.remove(this.indexFunction.apply(item));
        return item;
    }

    @Override
    public boolean remove(Object o) {
        this.index.remove(this.indexFunction.apply((E)o));
        return super.remove(o);
    }

    @Override
    public boolean contains(Object o) {
        return this.index.contains(o);
    }

    @Override
    public void clear() {
        super.clear();
        this.index.clear();
    }

    @Override
    public boolean add(E e) {
        this.index.add(this.indexFunction.apply(e));
        return super.add(e);
    }

    @Override
    public E remove() {
        E head = super.remove();
        this.index.remove(this.indexFunction.apply(head));
        return head;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return super.addAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        final Iterator<E> iterator = super.iterator();
        boolean ret = false;
        while (iterator.hasNext()) {
            E next = iterator.next();
            if (filter.test(next)) {
                this.index.remove(this.indexFunction.apply(next));
                iterator.remove();
                ret = true;
            }
        }
        return ret;
    }
}
