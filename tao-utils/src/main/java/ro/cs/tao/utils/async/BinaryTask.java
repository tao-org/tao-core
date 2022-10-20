package ro.cs.tao.utils.async;

import java.util.function.BiConsumer;

/**
 * Wrapper for a runnable that would eventually invoke a callback method accepting two arguments.
 *
 * @param <T>   The type of the runnable input and of the first argument of the callback method
 * @param <V>   The type of the second argument of the callback method
 *
 * @author  Cosmin Cara
 * @since   1.0
 */
public abstract class BinaryTask<T, V> implements Runnable {
    private final T reference;
    private final BiConsumer<T, V> callback;

    public BinaryTask(T ref, BiConsumer<T, V> callback) {
        if (ref == null) {
            throw new IllegalArgumentException("[ref] cannot be null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("[callback] cannot be null");
        }
        this.reference = ref;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            this.callback.accept(this.reference, execute(this.reference));
        } catch (Throwable t) {
            this.callback.accept(this.reference, null);
        }
    }

    public abstract V execute(T ref);
}
