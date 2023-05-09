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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Base class for notifiable components that receive messages into a fixed size queue.
 *
 * @author Cosmin Cara
 */
public abstract class NotifiableComponent extends Notifiable {
    protected static final int DEFAULT_QUEUE_SIZE = 100;
    protected int queueSize;
    protected final Queue<Message> messageQueue;

    /**
     * Default constructor.
     */
    public NotifiableComponent() {
        this(DEFAULT_QUEUE_SIZE);
    }

    /**
     * Argument constructor
     * @param queueSize The size of the internal queue
     */
    public NotifiableComponent(int queueSize) {
        super();
        this.queueSize = queueSize;
        this.messageQueue = new LinkedList<>();
        subscribe(topics());
    }

    /**
     * Returns the topics to which this component subscribes
     */
    protected abstract String[] topics();

    protected void setQueueSize(int newSize) { this.queueSize = newSize; }

    @Override
    protected void onMessageReceived(Message message) {
        synchronized (this.messageQueue) {
            if (this.messageQueue.size() == this.queueSize) {
                this.messageQueue.poll();
            }
            this.messageQueue.offer(message);
        }
    }

    /**
     * Returns the last message received and removes it from the internal queue.
     * If no message was present, returns <code>null</code>.
     */
    protected Message getLastMessage() {
        return this.messageQueue.poll();
    }
    /**
     * Returns all the messages from the internal queue and clears the queue.
     */
    protected List<Message> getLastMessages() {
        synchronized (this.messageQueue) {
            try {
                return new ArrayList<>(this.messageQueue);
            } finally {
                this.messageQueue.clear();
            }
        }
    }
}
