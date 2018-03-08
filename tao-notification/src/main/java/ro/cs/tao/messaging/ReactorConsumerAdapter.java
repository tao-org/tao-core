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

import reactor.bus.Event;

import java.util.function.Consumer;

/**
 * @author Cosmin Cara
 */
public class ReactorConsumerAdapter implements reactor.fn.Consumer<Event<Message>> {
    private final Consumer<Event<Message>> consumer;

    public static reactor.fn.Consumer<Event<Message>> wrap(Consumer<Event<Message>> consumer) {
        return new ReactorConsumerAdapter(consumer);
    }

    private ReactorConsumerAdapter(Consumer<Event<Message>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void accept(Event<Message> messageEvent) {
        this.consumer.accept(messageEvent);
    }
}
