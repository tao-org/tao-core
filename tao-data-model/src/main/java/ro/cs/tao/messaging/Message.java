package ro.cs.tao.messaging;

import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.Serializer;
import ro.cs.tao.serialization.SerializerFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.beans.Transient;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "message")
public class Message {
    public static final String PRINCIPAL_KEY = "Principal";
    public static final String PAYLOAD_KEY = "Payload";
    private static Serializer<Message, String> serializer;
    private long timestamp;
    private boolean read;
    private Object source;
    private Map<String, Object> data;

    static {
        try {
            serializer = SerializerFactory.create(Message.class, MediaType.JSON);
        } catch (SerializationException e) {
            e.printStackTrace();
        }
    }

    public static Message create(Principal principal, Object source, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put(PRINCIPAL_KEY, principal);
        data.put(PAYLOAD_KEY, message);
        return new Message(System.currentTimeMillis(), source, data);
    }

    public Message() { }

    public Message(long timestamp, Object source, Map<String, Object> data) {
        this.timestamp = timestamp;
        this.source = source;
        this.data = data;
        this.read = false;
    }

    @XmlElement(name = "isRead")
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    @XmlElement(name = "timestamp")
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Transient
    public Object getSource() { return source; }
    public void setSource(Object source) { this.source = source; }

    @XmlElement(name = "data")
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public Object getItem(String key) {
        return data != null ? data.get(key) : null;
    }
    public void addItem(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
    }

    @Override
    public String toString() {
        try {
            return serializer != null ? serializer.serialize(this) : super.toString();
        } catch (SerializationException e) {
            return super.toString();
        }
    }
}
