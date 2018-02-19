package ro.cs.tao.messaging;

import reactor.bus.Event;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public abstract class Notifiable implements Consumer<Event<Message>> {
    private String[] topics;
    protected final Logger logger;

    public Notifiable() {
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    @Override
    public void accept(Event<Message> messageEvent) {
        final Message payload = messageEvent.getData();
        logger.finest("Notification received: " + payload.toString());
        onMessageReceived(payload);
    }

    protected void subscribe(String... topics) {
        Messaging.subscribe(this, topics);
    }

    protected abstract void onMessageReceived(Message message);

}
