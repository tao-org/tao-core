package ro.cs.tao.messaging;

/**
 * The EventHandler interface provides a mechanism for handling events of a specified type.
 * Implementations of this interface should define the logic for processing events
 * by implementing the {@code handleEvent} method.
 *
 * @param <T> The type of the event to be handled.
 */
public interface EventHandler<T> {

    /**
     * Handles the given event by executing the associated logic for processing it.
     * Implementations of this method should define how the specified event is handled.
     *
     * @param event the event to be processed; must not be null
     * @param <T>   the type of the event being handled
     */
    void handleEvent(T event);
}
