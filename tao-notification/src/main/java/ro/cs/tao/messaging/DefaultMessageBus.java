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

import reactor.Environment;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.core.config.DispatcherType;
import ro.cs.tao.utils.executors.NamedThreadPoolExecutor;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ExecutorService;
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
    private final Map<Pattern, Set<reactor.fn.Consumer<Event<Message>>>> patternSubscribers;
    private final Set<String> matchedTopics;
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
        this.patternSubscribers = new HashMap<>();
        this.matchedTopics = new HashSet<>();
        this.executorService = new NamedThreadPoolExecutor("messaging-thread", 1);//Executors.newSingleThreadExecutor();
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
    public void subscribe(Consumer<Event<Message>> subscriber, Pattern topicPattern) {
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber cannot be null");
        }
        if (topicPattern == null) {
            throw new IllegalArgumentException("Invalid topic pattern");
        }
        reactor.fn.Consumer<Event<Message>> eventConsumer = ReactorConsumerAdapter.wrap(subscriber);
        List<String> matched = this.topics.stream().filter(t -> topicPattern.matcher(t).matches()).collect(Collectors.toList());
        for (String topic : matched) {
            this.messageBus.on($(topic), eventConsumer);
        }
        if (!this.patternSubscribers.containsKey(topicPattern)) {
            this.patternSubscribers.put(topicPattern, new HashSet<>());
        }
        this.patternSubscribers.get(topicPattern).add(eventConsumer);
    }

    @Override
    public void close(String... topics) {
        if (topics == null) {
            return;
        }
        for (String topic : topics) {
            this.messageBus.getConsumerRegistry().unregister(topic);
            this.matchedTopics.remove(topic);
        }
    }

    @Override
    public void send(Principal principal, String topic, Event<Message> event) {
        checkPatternConsumers(topic);
        event.getData().setTopic(topic);
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

    private void checkPatternConsumers(String topic) {
        if (!this.matchedTopics.contains(topic)) {
            Set<Set<reactor.fn.Consumer<Event<Message>>>> set = this.patternSubscribers.entrySet().stream()
                    .filter(e -> e.getKey().matcher(topic).matches())
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toSet());
            if (set != null && set.size() > 0) {
                set.forEach(sub -> sub.forEach(s -> this.messageBus.on($(topic), s)));
                this.matchedTopics.add(topic);
            }
        }
    }

}
