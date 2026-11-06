package ro.cs.tao.messaging.external;

import ro.cs.tao.messaging.EventHandler;
import ro.cs.tao.messaging.ExternalEventPubSub;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.util.Set;
import java.util.logging.Logger;

/**
 * The ExternalEventsDispatcher is a singleton class responsible for managing
 * the dispatching of external events across subscribed services and components.
 * It allows for centralized control in subscribing to and emitting events
 * through a predefined set of ExternalEventPubSub subscribers.
 *
 * @author Cosmin Cara
 */
public class ExternalEventsDispatcher {

    private static ExternalEventsDispatcher instance;
    private final Set<ExternalEventPubSub> subscribers;
    private EventHandler eventHandler;

    static {
        instance = new ExternalEventsDispatcher();
    }

    /**
     * Provides access to the singleton instance of the {@code ExternalEventsDispatcher}.
     *
     * The {@code ExternalEventsDispatcher} is responsible for managing external event
     * subscriptions and message dispatching across services. This method ensures that
     * only one instance of the dispatcher is created and shared throughout the application.
     *
     * @return the singleton instance of {@code ExternalEventsDispatcher}
     */
    public static ExternalEventsDispatcher getInstance() {
        return instance;
    }

    private ExternalEventsDispatcher() {
        this.subscribers = ServiceRegistryManager.getInstance().getServiceRegistry(ExternalEventPubSub.class).getServices();
    }

    /**
     * Adds an event handler to the dispatcher and assigns it to all subscribed components.
     * This method ensures that each subscriber is updated with the provided event handler
     * and initiates the subscription process for receiving external events.
     * If an error occurs during the subscription or handler assignment, it logs the issue.
     *
     * @param handler the {@link EventHandler} to process events; must not be null
     */
    public void addEventHandler(EventHandler handler) {
        this.eventHandler = handler;
        for (ExternalEventPubSub subscriber : this.subscribers) {
            try {
                subscriber.setEventHandler(this.eventHandler);
                subscriber.subscribe();
            } catch (Throwable t) {
                Logger.getLogger(ExternalEventsDispatcher.class.getName()).warning(t.getMessage());
            }
        }
    }

    /**
     * Dispatches a given event to all subscribed {@link ExternalEventPubSub} instances.
     * Iterates through the list of subscribers and invokes their {@code emit} method
     * to handle the provided event. Any errors encountered during the dispatch process
     * are logged as warnings.
     *
     * @param event the event to be dispatched to subscribers; must not be null
     */
    public void send(Object event) {
        for (ExternalEventPubSub subscriber : this.subscribers) {
            try {
                subscriber.emit(event);
            } catch (Throwable t) {
                Logger.getLogger(ExternalEventsDispatcher.class.getName()).warning(t.getMessage());
            }
        }
    }
}
