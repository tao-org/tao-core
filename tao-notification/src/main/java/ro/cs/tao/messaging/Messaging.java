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

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
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

    public static void subscribe(Consumer subscriber, String... topics) {
        instance.subscribe(subscriber, topics);
    }

    public static void subscribe(Consumer subscriber, Pattern topicPattern) {
        instance.subscribe(subscriber, topicPattern);
    }

    public static void close(String... topics) {
        instance.close(topics);
    }

    public static void setPersister(MessagePersister persister) {
        instance.setPersister(persister);
    }

    public static <T extends Serializable> void send(Principal principal, String topic, T event) {
        instance.send(principal, topic, event);
    }

    public static void send(Principal principal, String topic, Message message) {
        instance.send(principal, topic, message);
    }

    public static void send(Principal principal, String topic, Object source, String message) {
        instance.send(principal, topic, source, message);
    }

    public static void shutdown() {
        instance.shutdown();
    }
}
