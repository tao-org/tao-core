/*
 * Copyright (C) 2017 CS ROMANIA
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
 * @author Cosmin Cara
 */
public interface EventBus<T extends Serializable> {
    void subscribe(Consumer<T> subscriber, String... topics);
    void subscribe(Consumer<T> subscriber, Pattern topicPattern);
    void close(String... topics);
    void setPersister(MessagePersister persister);
    void send(Principal principal, String topic, T event);
    void send(Principal principal, String topic, Message message);
    default void send(Principal principal, String topic, Object source, String message) {
        send(principal, topic, Message.create(principal.getName(), source, message));
    }
    void shutdown();
}
