package ro.cs.tao.messaging;

import ro.cs.tao.utils.StringUtilities;

/**
 * Interface that should be implemented by components that wish to send messages.
 *
 * @author Cosmin Cara
 */
public interface Emitter {

    /**
     * Sends a message object. Note that if the message should not be persistent,
     * Message.setPersistent(false) should be set previously.
     * @param message   The message
     */
    default void send(Message message) {
        if (StringUtilities.isNullOrEmpty(message.getUserId())) {
            throw new RuntimeException("No userId set");
        }
        if (StringUtilities.isNullOrEmpty(message.getTopic())) {
            throw new RuntimeException("No topic set");
        }
        Messaging.send(message);
    }
    /**
     * Sends a message object to the given topic. Note that if the message should not be persistent,
     * Message.setPersistent(false) should be set previously.
     *
     * @param topic     The topic
     * @param message   The message
     */
    default void send(Topic topic, Message message) {
        if (message.getUserId() != null) {
            throw new RuntimeException("No userId set");
        }
        message.setTopic(topic.value());
        Messaging.send(message.getUserId(), topic.value(), message);
    }
    /**
     * Sends a text message to the given topic and on behalf of toe given user. Note that if the message should not be persistent,
     * Message.setPersistent(false) should be set previously.
     *
     * @param topic     The topic
     * @param userId    The user identifier
     * @param message   The message
     */
    default void send(Topic topic, String userId, String message) {
        if (StringUtilities.isNullOrEmpty(userId)) {
            throw new RuntimeException("No userId set");
        }
        Messaging.send(userId, topic.value(), message);
    }

}
