package ro.cs.tao.messaging;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;
import java.util.function.Consumer;

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

    public static void subscribe(Consumer subscriber, String topicPattern) {
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
