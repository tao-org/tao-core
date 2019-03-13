package ro.cs.tao.utils.executors.monitoring;

import ro.cs.tao.utils.executors.OutputConsumer;

import java.util.ArrayList;
import java.util.List;

public class SimpleConsumer implements OutputConsumer {
    private List<String> messages = new ArrayList<>();
    @Override
    public void consume(String message) {
        messages.add(message.replace("\r", ""));
    }
    List<String> getMessages() { return this.messages; }
}
