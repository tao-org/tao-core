package ro.cs.tao.utils;

import java.util.Objects;

/**
 * Represents an operation that accepts three input arguments and returns no result.
 * This is a functional interface whose functional method is {@code accept(T, U, V)}.
 *
 * The operations represented by this interface are expected to be side-effecting as it does not return a result.
 *
 * @param <T> the type of the first argument to the operation
 * @param <U> the type of the second argument to the operation
 * @param <V> the type of the third argument to the operation
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {

    void accept(T t, U u, V v);

    default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after);
        return (t, u, v) -> {
            accept(t, u, v);
            after.accept(t, u, v);
        };
    }

}
