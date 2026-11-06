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

import ro.cs.tao.persistence.MessageProvider;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Implementation for an in-memory message bus.
 *
 * @author Cosmin Cara
 */
public class DefaultMessageBus implements ro.cs.tao.messaging.EventBus<MessageEvent> {
    private static final int MAX_THREADS = 2;

    private final ExecutorService executor;
    private final Map<String, Set<Consumer<MessageEvent>>> topicSubscribers;
    private final Map<Pattern, Set<Consumer<MessageEvent>>> patternSubscribers;
    private MessageProvider messagePersister;
    private final Logger logger;

    public DefaultMessageBus() {
        this.executor = Executors.newFixedThreadPool(MAX_THREADS);
        this.topicSubscribers = new ConcurrentHashMap<>();
        this.patternSubscribers = new ConcurrentHashMap<>();
        this.logger = Logger.getLogger(DefaultMessageBus.class.getName());
    }

    @Override
    public void shutdown() {
        this.executor.shutdown();
    }

    @Override
    public void setPersister(MessageProvider messagePersister) {
        this.messagePersister = messagePersister;
    }

    @Override
    public void subscribe(Consumer<MessageEvent> subscriber, String... topics) {
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber cannot be null");
        }
        if (topics == null || topics.length == 0) {
            throw new IllegalArgumentException("At least one topic should be given");
        }
        for (String topic : topics) {
            this.topicSubscribers.computeIfAbsent(topic, t -> new CopyOnWriteArraySet<>()).add(subscriber);
        }
    }

    @Override
    public void subscribe(Consumer<MessageEvent> subscriber, Pattern topicPattern) {
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber cannot be null");
        }
        if (topicPattern == null) {
            throw new IllegalArgumentException("Invalid topic pattern");
        }
        this.patternSubscribers.computeIfAbsent(topicPattern, p -> new CopyOnWriteArraySet<>()).add(subscriber);
    }

    @Override
    public void close(String... topics) {
        if (topics == null) {
            return;
        }
        Arrays.stream(topics).forEach(this.topicSubscribers::remove);
    }

    @Override
    public void send(String principal, String topic, MessageEvent event) {
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
        Set<Consumer<MessageEvent>> subscribers = new LinkedHashSet<>();
        Set<Consumer<MessageEvent>> directSubscribers = this.topicSubscribers.get(topic);
        if (directSubscribers != null) {
            subscribers.addAll(directSubscribers);
        }
        this.patternSubscribers.entrySet().stream()
                .filter(e -> e.getKey().matcher(topic).matches())
                .map(Map.Entry::getValue)
                .forEach(subscribers::addAll);
        subscribers.forEach(subscriber -> this.executor.submit(() -> {
            try {
                subscriber.accept(event);
            } catch (Exception ex) {
                this.logger.severe(ex.getMessage());
            }
        }));
    }

    @Override
    public void send(String principal, String topic, Message message) {
        send(principal, topic, MessageEvent.wrap(message));
    }

}
