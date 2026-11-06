package ro.cs.tao.messaging;

import java.io.Serial;
import java.io.Serializable;

/**
 * Lightweight event wrapper for message bus payloads.
 */
public final class MessageEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Message data;

    private MessageEvent(Message data) {
        this.data = data;
    }

    public static MessageEvent wrap(Message data) {
        return new MessageEvent(data);
    }

    public Message getData() {
        return data;
    }
}
