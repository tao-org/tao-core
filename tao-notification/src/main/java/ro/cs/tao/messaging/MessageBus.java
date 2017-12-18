package ro.cs.tao.messaging;

import reactor.Environment;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.core.config.DispatcherType;
import reactor.fn.Consumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

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
    }

    private final EventBus messageBus;
    private MessagePersister messagePersister;
    private final ExecutorService executorService;
    private final Logger logger;

    private MessageBus() {
        Environment environment = Environment.initializeIfEmpty();
        this.messageBus = EventBus.create(environment,
                                          Environment.newDispatcher(MAX_THREADS,
                                                                    MAX_THREADS,
                                                                    DispatcherType.THREAD_POOL_EXECUTOR));
        this.executorService = Executors.newSingleThreadExecutor();
        this.logger = Logger.getLogger(MessageBus.class.getName());
    }

    private void exit() {
        Environment.terminate();
    }

    public static void registerPersister(MessagePersister messagePersister) {
        instance.messagePersister = messagePersister;
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
        final Message msg = new Message(System.currentTimeMillis(), userId, source.toString(), message);
        instance.messageBus.notify(topic, Event.wrap(msg));
        if (instance.messagePersister != null) {
            instance.executorService.submit(() -> {
                try {
                    if (instance.messagePersister != null) {
                        instance.messagePersister.saveMessage(msg);
                    }
                } catch (Exception e) {
                    instance.logger.severe(e.getMessage());
                }
            });
        }
    }

}
