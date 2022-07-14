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

import reactor.bus.Event;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Base class that should be extended by all components that wish to receive messages.
 *
 * @author Cosmin Cara
 */
public abstract class Notifiable implements Consumer<Event<Message>> {
    protected final Logger logger;

    public Notifiable() {
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void accept(Event<Message> messageEvent) {
        final Message payload = messageEvent.getData();
        onMessageReceived(payload);
    }

    /**
     * Subscribes this instance to one or more topics
     * @param topics    The topic(s) to subscribe to
     */
    protected void subscribe(String... topics) {
        Messaging.subscribe(this, topics);
    }

    /**
     * Handles the dispatch of a message to this instance
     * @param message   The message
     */
    protected abstract void onMessageReceived(Message message);

}
