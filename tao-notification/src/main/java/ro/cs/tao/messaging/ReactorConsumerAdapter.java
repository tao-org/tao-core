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
