package ro.cs.tao.services.operations;

import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lets an operation notify the user interface while it is still executing (e.g. before starting a long query),
 * as opposed to the result, which is available only when the operation completes.
 * The listener is bound by the caller to the thread executing the operation; when no listener is bound,
 * the published events are discarded.
 */
public final class OperationEvents {
    private static final Logger logger = Logger.getLogger(OperationEvents.class.getName());
    private static final ThreadLocal<BiConsumer<String, Object>> listener = new ThreadLocal<>();

    private OperationEvents() {
    }

    /**
     * Publishes an event to the listener bound to the current thread, if any.
     *
     * @param type      the event type, e.g. "datasources.search.started"
     * @param payload   the event data
     */
    public static void publish(String type, Object payload) {
        final BiConsumer<String, Object> current = listener.get();
        if (current != null && type != null) {
            try {
                current.accept(type, payload);
            } catch (RuntimeException e) {
                // a notification failure must not fail the operation
                logger.log(Level.WARNING, "Cannot publish operation event " + type + ": " + e.getMessage());
            }
        }
    }

    /**
     * Binds a listener to the current thread. Must be paired with {@link #unbind()}.
     *
     * @param eventListener receives the event type and payload
     */
    public static void bind(BiConsumer<String, Object> eventListener) {
        listener.set(eventListener);
    }

    /**
     * Removes the listener bound to the current thread.
     */
    public static void unbind() {
        listener.remove();
    }
}
