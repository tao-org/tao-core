package ro.cs.tao.messaging;

import reactor.bus.Event;
import reactor.fn.Consumer;

import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public abstract class NotifiableComponent implements Consumer<Event<Message>> {
    private String[] topics;
    protected final Logger logger;

    public NotifiableComponent() {
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void accept(Event<Message> messageEvent) {
        final Message payload = messageEvent.getData();
        logger.finest("Notification received: " + payload.toString());
        onMessageReceived(payload);
    }

    protected void subscribe(String... topics) {
        MessageBus.register(this, topics);
    }

    protected abstract void onMessageReceived(Message message);

}
