package ro.cs.tao.messaging;

import reactor.Environment;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.core.config.DispatcherType;
import reactor.fn.Consumer;

import static reactor.bus.selector.Selectors.$;

/**
 * @author Cosmin Cara
 */
public class MessageBus {
    public static final String INFORMATION = "info";
    public static final String WARNING = "warn";
    public static final String ERROR = "error";
    public static final String PROGRESS = "progress";

    private static final int MAX_THREADS = 2;
    private static final MessageBus instance;

    static {
        instance = new MessageBus();
        instance.initialize();
    }

    private EventBus messageBus;

    private MessageBus() { }

    private void initialize() {
        Environment environment = Environment.initializeIfEmpty();
        this.messageBus = EventBus.create(environment,
                                          Environment.newDispatcher(MAX_THREADS,
                                                                    MAX_THREADS,
                                                                    DispatcherType.THREAD_POOL_EXECUTOR));
    }

    private void exit() {
        Environment.terminate();
    }

    public static void register(Consumer<Event<Message>> subscriber, String... topics) {
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber cannot be null");
        }
        if (topics == null || topics.length == 0) {
            throw new IllegalArgumentException("At least one topic should be given");
        }
        for (String topic : topics) {
            instance.messageBus.on($(topic), subscriber);
        }
    }

    public static void close(String... topics) {
        if (topics == null) {
            return;
        }
        for (String topic : topics) {
            instance.messageBus.getConsumerRegistry().unregister(topic);
        }
    }

    public static void send(int userId, String topic, Object source, String message) {
        Message msg = new Message(System.nanoTime(), userId, source.toString(), message);
        instance.messageBus.notify(topic, Event.wrap(msg));
    }

}
