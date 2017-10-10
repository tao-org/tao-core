package ro.cs.tao.utils.async;

import java.util.function.BiConsumer;

/**
 * @author Cosmin Cara
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
