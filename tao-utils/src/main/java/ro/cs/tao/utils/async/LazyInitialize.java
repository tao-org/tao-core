package ro.cs.tao.utils.async;

import java.util.function.Supplier;

/**
 * A utility class that provides a thread-safe lazy initialization mechanism.
 * This class initializes a value using a provided {@link Supplier} when the value
 * is first accessed via the {@link #get()} method and then caches the result
 * for all subsequent accesses.
 *
 * @param <T> The type of the object to be lazily initialized
 *
 * @author Cosmin Cara
 */
public class LazyInitialize<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private Supplier<T> current;

    public static <T> LazyInitialize<T> using(Supplier<T> supplier) {
        return new LazyInitialize<>(supplier);
    }

    public LazyInitialize(Supplier<T> supplier) {
        this.supplier = supplier;
        this.current = this::swap;
    }

    @Override
    public T get() {
        return this.current.get();
    }

    private T swap() {
        T instance = supplier.get();
        current = () -> instance;
        return instance;
    }
}
