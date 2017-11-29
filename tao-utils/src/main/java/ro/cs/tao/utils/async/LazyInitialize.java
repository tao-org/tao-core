package ro.cs.tao.utils.async;

import java.util.function.Supplier;

/**
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
