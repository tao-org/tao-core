package ro.cs.tao.messaging;

import ro.cs.tao.security.SystemPrincipal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Represents a generic subscriber and publisher for external events.
 *
 * This class provides methods for subscribing to an external event
 * source and handling messages received from that source. The class
 * also includes a default method to translate the received message into
 * a standardized {@link Message} object for further processing.
 *
 * @param <M> The type of messages expected by the subscriber.
 */
public abstract class ExternalEventPubSub<M> {

    protected EventHandler<M> handler;
    protected final ExecutorService executorService;

    public ExternalEventPubSub() {
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Subscribes to an external event source. This method is responsible for
     * initiating the subscription process, allowing the subscriber to start
     * receiving messages or notifications from the specified event source.
     * Implementers of this method should define the specific behavior of
     * establishing the subscription, such as connecting to a messaging system
     * or registering with a remote service.
     *
     * Once subscribed, the messages received from the event source can be
     * processed using the {@link #onMessage(Object)} method.
     */
    public abstract void subscribe();

    /**
     * Emits a message to a specific event source. This method should be overridden by the subclass
     * for sending the provided message, allowing it to be processed, propagated, or
     * published as per the implementation details.
     *
     * @param message The message to be emitted, of type {@code M}.
     */
    public void emit(M message) {
        this.executorService.submit(() -> { ExternalEventPubSub.this.sendMessage(message); });
    }

    public void setEventHandler(EventHandler<M> handler) {
        this.handler = handler;
    }

    /**
     * Handles the incoming message received from an external event source.
     * Implementations of this method should define the behavior for processing
     * the provided message.
     *
     * @param message The message received from the external event source, of type {@code M}.
     */
    public void onMessage(M message) {
        if (this.handler != null) {
            this.executorService.submit(() -> { ExternalEventPubSub.this.handler.handleEvent(message); });
        }
    }

    /**
     * Sends a message to a specific external destination or event source.
     *
     * This method must be implemented by subclasses to define the logic for
     * dispatching the provided message. It is invoked internally by the
     * {@link #emit(Object)} method when a message needs to be sent. The exact
     * behavior of this method depends on the concrete implementation, such as
     * interfacing with a messaging system, invoking an API, or other forms of
     * message transmission.
     *
     * @param message The message to be sent, of type {@code M}.
     */
    protected abstract void sendMessage(M message);

    /**
     * Converts an input message of type {@code M} into a {@link Message} object.
     * This method transforms the input message by assigning it a user ID,
     * timestamp, payload, and topic, preparing it for further processing
     * or propagation.
     *
     * @param message The input message of type {@code M} to be converted into a {@link Message}.
     * @return A {@link Message} object containing the transformed data from the input message.
     */
    protected Message translate(M message) {
        return new Message() {{
           setUserId(SystemPrincipal.instance().getName());
           setTimestamp(System.currentTimeMillis());
           setPayload(String.valueOf(message));
           setTopic(Topic.EXTERNAL_EVENT.value());
        }};
    }

}
