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

import reactor.Environment;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.core.Dispatcher;
import reactor.core.dispatch.ThreadPoolExecutorDispatcher;
import ro.cs.tao.persistence.MessageProvider;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static reactor.bus.selector.Selectors.$;

/**
 * Implementation for an in-memory message bus relying on the Reactor {@link EventBus}.
 *
 * @author Cosmin Cara
 */
public class DefaultMessageBus implements ro.cs.tao.messaging.EventBus<Event<Message>> {
    private static final int MAX_THREADS = 2;
    private static final int CAPACITY = 1024;

    private final EventBus messageBus;
    private final Set<String> topics;
    private final Map<String, Set<reactor.fn.Consumer<Event<Message>>>> patternSubscribers;
    private final Set<String> matchedTopics;
    private MessageProvider messagePersister;
    private final Logger logger;

    public DefaultMessageBus() {
        Environment environment = Environment.initializeIfEmpty();
        Dispatcher dispatcher = new ThreadPoolExecutorDispatcher(MAX_THREADS, CAPACITY, Executors.newCachedThreadPool());
        this.messageBus = EventBus.create(environment,
                                          dispatcher
                                          /*Environment.newDispatcher(CAPACITY,
                                                                    MAX_THREADS,
                                                                    DispatcherType.THREAD_POOL_EXECUTOR)*/);
        this.topics = new HashSet<>();
        this.patternSubscribers = new HashMap<>();
        this.matchedTopics = new HashSet<>();
        this.logger = Logger.getLogger(DefaultMessageBus.class.getName());
    }

    @Override
    public void shutdown() {
        Environment.terminate();
    }

    @Override
    public void setPersister(MessageProvider messagePersister) {
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
        if (!this.patternSubscribers.containsKey(topicPattern.pattern())) {
            this.patternSubscribers.put(topicPattern.pattern(), new HashSet<>());
        }
        this.patternSubscribers.get(topicPattern.pattern()).add(eventConsumer);
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
    public void send(String principal, String topic, Event<Message> event) {
        checkPatternConsumers(topic);
        Message message = event.getData();
        message.setTopic(topic);
        message.setUserId(principal);
        try {
            if (messagePersister != null && message.isPersistent()) {
                message.setId(null);
                this.messagePersister.save(message);
            }
        } catch (Exception e) {
            this.logger.severe(e.getMessage());
        }
        this.messageBus.notify(topic, event);
    }

    @Override
    public void send(String principal, String topic, Message message) {
        message.setUserId(principal);
        send(principal, topic, Event.wrap(message));
    }

    private void checkPatternConsumers(String topic) {
        if (!this.matchedTopics.contains(topic)) {
            Set<Set<reactor.fn.Consumer<Event<Message>>>> set = this.patternSubscribers.entrySet().stream()
                    .filter(e -> Pattern.compile(e.getKey()).matcher(topic).matches())
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toSet());
            if (!set.isEmpty()) {
                set.forEach(sub -> sub.forEach(s -> this.messageBus.on($(topic), s)));
                this.matchedTopics.add(topic);
            }
        }
    }

}
