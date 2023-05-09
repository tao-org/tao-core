package ro.cs.tao.utils.async;

import java.util.function.Consumer;

/**
 * Wrapper for a runnable that would eventually invoke a callback method accepting one argument.
 *
 * @param <T>   The type of the runnable input and of the argument of the callback method
 *
 * @author  Cosmin Cara
 * @since   1.0
 */
public abstract class UnaryTask<T> implements Runnable {
    private final Consumer<T> callback;

    public UnaryTask(Consumer<T> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("[callback] cannot be null");
        }
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            this.callback.accept(execute());
        } catch (Throwable t) {
            this.callback.accept(null);
        }
    }

    public abstract T execute();
}
