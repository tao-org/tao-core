/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.messaging;

import java.io.Serializable;
import java.security.Principal;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Interface that abstracts the messaging (i.e. event) bus used for communication between various framework components.
 *
 * @author Cosmin Cara
 */
public interface EventBus<T extends Serializable> {
    /**
     * Subscribes the given consumer to the given topics.
     * @param subscriber    The subscriber
     * @param topics        The topics
     */
    void subscribe(Consumer<T> subscriber, String... topics);

    /**
     * Subscribes the given consumer to topics that match the given pattern.
     * @param subscriber   The subscriber
     * @param topicPattern  The topic pattern
     */
    void subscribe(Consumer<T> subscriber, Pattern topicPattern);

    /**
     * Closes the given topics.
     * @param topics    The topics to close.
     */
    void close(String... topics);

    /**
     * Assigns a persister to this event bus such that any message will be stored.
     * @param persister The message persister.
     */
    void setPersister(MessagePersister persister);

    /**
     * Sends an event on a topic.
     * @param principal The user under which identity the event is send.
     * @param topic The topic
     * @param event The event
     */
    void send(Principal principal, String topic, T event);

    /**
     * Sends a message on a topic
     * @param principal The user under which identity the event is send.
     * @param topic The topic
     * @param message   The message
     */
    void send(Principal principal, String topic, Message message);

    /**
     * Sends a message on a topic indicating also the source of the message.
     * @param principal The user under which identity the event is send.
     * @param topic The topic
     * @param source    The source of the message
     * @param message   The message
     */
    default void send(Principal principal, String topic, Object source, String message) {
        send(principal, topic, Message.create(principal.getName(), source, message, true));
    }

    /**
     * Sends a message on a topic indicating also the source of the message.
     * @param principal The user under which identity the event is send.
     * @param topic The topic
     * @param source    The source of the message
     * @param message   The message
     * @param persistent If the message should be persisted in the message store or not
     */
    default void send(Principal principal, String topic, Object source, String message, boolean persistent) {
        send(principal, topic, Message.create(principal.getName(), source, message, persistent));
    }

    default void send(Principal principal, String topic, Message message, boolean persistent) {
        message.setPersistent(persistent);
        send(principal, topic, message);
    }

    /**
     * Instructs the event bus to shutdown.
     */
    void shutdown();
}
