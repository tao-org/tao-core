package ro.cs.tao.utils;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Very simple implementation of a queue.
 *
 * @param <T>   The type of the queue elements
 */
public class FixedQueue<T> {
    private final T[] elements;

    public FixedQueue(Class<T> clazz, int size) {
        elements = (T[]) Array.newInstance(clazz, size);
    }

    /**
     * Pushes an element to the queue and removes the current head of the queue.
     *
     * @param value The element to add
     */
    public T enqueue(T value) {
        T head = null;
        if (elements.length == 1) {
            head = elements[0];
        } else {
            for (int i = elements.length - 1; i > 0; i--) {
                if (i == elements.length - 1) {
                    head = elements[i];
                }
                elements[i] = elements[i - 1];
            }
        }
        elements[0] = value;
        return head;
    }

    /**
     * Removes the head of the queue.
     */
    public T dequeue() { return enqueue(null); }

    /**
     * Returns the head of the queue, but does not remove it.
     */
    public T peek() { return elements[0]; }

    /**
     * Empties the queue (i.e. nullifies all its elements)
     */
    public void clear() { Arrays.fill(elements, null); }
}