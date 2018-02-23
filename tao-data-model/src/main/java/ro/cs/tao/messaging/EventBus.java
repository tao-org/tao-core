package ro.cs.tao.messaging;

import java.io.Serializable;
import java.security.Principal;
import java.util.function.Consumer;

/**
 * @author Cosmin Cara
 */
public interface EventBus<T extends Serializable> {
    void subscribe(Consumer<T> subscriber, String... topics);
    void subscribe(Consumer<T> subscriber, String topicPattern);
    void close(String... topics);
    void setPersister(MessagePersister persister);
    void send(Principal principal, String topic, T event);
    void send(Principal principal, String topic, Message message);
    default void send(Principal principal, String topic, Object source, String message) {
        send(principal, topic, Message.create(principal.getName(), source, message));
    }
    void shutdown();
}
