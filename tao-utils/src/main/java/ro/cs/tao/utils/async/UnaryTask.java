package ro.cs.tao.utils.async;

import java.util.function.Consumer;

/**
 * @author Cosmin Cara
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
