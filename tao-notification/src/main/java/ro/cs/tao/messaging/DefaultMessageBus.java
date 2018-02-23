package ro.cs.tao.messaging;

import reactor.Environment;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.core.config.DispatcherType;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static reactor.bus.selector.Selectors.$;

/**
 * @author Cosmin Cara
 */
public class DefaultMessageBus implements ro.cs.tao.messaging.EventBus<Event<Message>> {
    private static final int MAX_THREADS = 2;

    private final EventBus messageBus;
    private final Set<String> topics;
    private MessagePersister messagePersister;
    private final ExecutorService executorService;
    private final Logger logger;

    public DefaultMessageBus() {
        Environment environment = Environment.initializeIfEmpty();
        this.messageBus = EventBus.create(environment,
                                          Environment.newDispatcher(MAX_THREADS,
                                                                    MAX_THREADS,
                                                                    DispatcherType.THREAD_POOL_EXECUTOR));
        this.topics = new HashSet<>();
        this.executorService = Executors.newSingleThreadExecutor();
        this.logger = Logger.getLogger(DefaultMessageBus.class.getName());
    }

    @Override
    public void shutdown() {
        Environment.terminate();
    }

    @Override
    public void setPersister(MessagePersister messagePersister) {
        this.messagePersister = messagePersister;
    }

    @Override
    public void subscribe(Consumer<Event<Message>> subscriber, String... topics) {
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber cannot be null");
        }
        if (topics == null || topics.length == 0) {
            throw new IllegalArgumentException("At least one topic should be given");
        }
        for (String topic : topics) {
            this.topics.add(topic);
            this.messageBus.on($(topic), ReactorConsumerAdapter.wrap(subscriber));
        }
    }

    @Override
    public void subscribe(Consumer<Event<Message>> subscriber, String topicPattern) {
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber cannot be null");
        }
        if (topicPattern == null || topicPattern.isEmpty()) {
            throw new IllegalArgumentException("Invalid topic pattern");
        }
        Pattern pattern = Pattern.compile(topicPattern);
        List<String> matched = this.topics.stream().filter(t -> pattern.matcher(t).matches()).collect(Collectors.toList());
        for (String topic : matched) {
            this.messageBus.on($(topic), ReactorConsumerAdapter.wrap(subscriber));
        }
    }

    @Override
    public void close(String... topics) {
        if (topics == null) {
            return;
        }
        for (String topic : topics) {
            this.messageBus.getConsumerRegistry().unregister(topic);
        }
    }

    @Override
    public void send(Principal principal, String topic, Event<Message> event) {
        this.messageBus.notify(topic, event);
        if (this.messagePersister != null) {
            this.executorService.submit(() -> {
                try {
                    if (this.messagePersister != null) {
                        this.messagePersister.saveMessage(event.getData());
                    }
                } catch (Exception e) {
                    this.logger.severe(e.getMessage());
                }
            });
        }
    }

    @Override
    public void send(Principal principal, String topic, Message message) {
        send(principal, topic, Event.wrap(message));
    }

}
