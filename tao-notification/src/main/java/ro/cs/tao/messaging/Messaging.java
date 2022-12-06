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

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.persistence.MessageProvider;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Convenience class for communicating on the message bus.
 *
 * @author Cosmin Cara
 */
public class Messaging {
    private static EventBus instance;

    static {
        final ServiceRegistry<EventBus> registry = ServiceRegistryManager.getInstance().getServiceRegistry(EventBus.class);
        final Set<EventBus> services = registry.getServices();
        final String providerClassName = ConfigurationManager.getInstance().getValue("notification.provider");
        if (services.size() > 1 && providerClassName == null) {
            throw new IllegalArgumentException("Cannot accept more than one message bus. Please set the notification.provider property");
        } else if (providerClassName != null) {
            instance = registry.getService(providerClassName);
        } else if (services.size() == 1) {
            instance = services.iterator().next();
        }
        if (instance == null) {
            throw new InternalError("No message bus found.");
        }
    }
    /**
     * Registers a message consumer to one or more topics
     * @param subscriber    The consumer
     * @param topics        One or more topics to register to
     */
    public static void subscribe(Consumer subscriber, String... topics) {
        instance.subscribe(subscriber, topics);
    }
    /**
     * Registers a message consumer to topics that follow the given regex pattern
     * @param subscriber    The consumer
     * @param topicPattern  The topic regex pattern
     */
    public static void subscribe(Consumer subscriber, Pattern topicPattern) {
        instance.subscribe(subscriber, topicPattern);
    }
    /**
     * Closes one or more topics
     * @param topics    The topic(s) to be closed
     */
    public static void close(String... topics) {
        instance.close(topics);
    }
    /**
     * Registers a message provider that can persist messages
     * @param persister The message provider
     */
    public static void setPersister(MessageProvider persister) {
        instance.setPersister(persister);
    }
    /**
     * Sends a message with any serializable content
     * @param principal     The principal sending the message
     * @param topic         The topic to send the message to
     * @param event         The message information
     * @param <T>           The type of the message information
     */
    public static <T extends Serializable> void send(Principal principal, String topic, T event) {
        instance.send(principal, topic, event);
    }
    /**
     * Sends a simple text message
     * @param principal     The principal sending the message
     * @param topic         The topic to send the message to
     * @param message       The message
     */
    public static void send(Principal principal, String topic, String message) {
        instance.send(principal, topic, Message.create(principal.getName(), null, message));
    }
    /**
     * Sends a simple text message
     * @param principal     The principal sending the message
     * @param topic         The topic to send the message to
     * @param message       The message
     */
    public static void send(String principal, String topic, String message) {
        instance.send(principal, topic, Message.create(principal, null, message));
    }
    /**
     * Sends a structured message
     * @param principal     The principal sending the message
     * @param topic         The topic to send the message to
     * @param message       The structured message
     */
    public static void send(Principal principal, String topic, Message message) {
        instance.send(principal, topic, message);
    }
    /**
     * Sends a structured message
     * @param principal     The principal sending the message
     * @param topic         The topic to send the message to
     * @param message       The structured message
     */
    public static void send(String principal, String topic, Message message) {
        instance.send(principal, topic, message);
    }
    /**
     * Sends a structured message
     * @param principal     The principal sending the message
     * @param topic         The topic to send the message to
     * @param message       The structured message
     * @param persistent    If <code>true</code> the message will be persisted to the backend database
     */
    public static void send(Principal principal, String topic, Message message, boolean persistent) {
        instance.send(principal, topic, message, persistent);
    }

    /**
     * Sends a text message from the specified originator (source)
     * @param principal     The principal sending the message
     * @param topic         The topic to send the message to
     * @param source        The message originator (usually a service or a class instance)
     * @param message       The message
     */
    public static void send(Principal principal, String topic, Object source, String message) {
        instance.send(principal, topic, source, message);
    }
    /**
     * Sends a text message from the specified originator (source) with additional data
     * @param principal     The principal sending the message
     * @param topic         The topic to send the message to
     * @param source        The message originator (usually a service or a class instance)
     * @param message       The message
     * @param data          Additional data (preferably as a JSONified string)
     */
    public static void send(Principal principal, String topic, Object source, String message, String data) {
        instance.send(principal, topic, source, message, data);
    }
    /**
     * Sends a text message from the specified originator (source)
     * @param principal     The principal sending the message
     * @param topic         The topic to send the message to
     * @param source        The message originator (usually a service or a class instance)
     * @param message       The message
     * @param persist       If <code>true</code> the message will be persisted to the backend database
     */
    public static void send(Principal principal, String topic, Object source, String message, boolean persist) {
        instance.send(principal, topic, source, message, persist);
    }
    /**
     * Sends a structured message from the specified originator (source)
     * @param principal     The principal sending the message
     * @param topic         The topic to send the message to
     * @param source        The message originator (usually a service or a class instance)
     * @param message       The message
     * @param persist       If <code>true</code> the message will be persisted to the backend database
     */
    public static void send(Principal principal, String topic, Object source, Message message, boolean persist) {
        instance.send(principal, topic, message, persist);
    }
    /**
     * Shuts down the message bus
     */
    public static void shutdown() {
        instance.shutdown();
    }
}
