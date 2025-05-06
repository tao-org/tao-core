package ro.cs.tao.utils.executors.monitoring;

import ro.cs.tao.utils.executors.OutputConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple implementation of {@link OutputConsumer} that collects and stores
 * messages provided to the {@code consume} method.
 *
 * This class strips carriage return characters ('\\r') from each message before
 * adding it to an internal list. The stored messages can be retrieved later using
 * the {@code getMessages} method.
 */
public class SimpleConsumer implements OutputConsumer {
    private final List<String> messages = new ArrayList<>();
    @Override
    public void consume(String message) {
        messages.add(message.replace("\r", ""));
    }
    List<String> getMessages() { return this.messages; }
}
