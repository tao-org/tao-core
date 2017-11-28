package ro.cs.tao.messaging;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.beans.Transient;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "message")
public class Message {
    private long timestamp;
    private int userId;
    private boolean read;
    private Object source;
    private String data;

    public Message() { }

    public Message(long timestamp, int userId, Object source, String data) {
        this.timestamp = timestamp;
        this.source = source;
        this.data = data;
        this.userId = userId;
        this.read = false;
    }

    @XmlElement(name = "userId")
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

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
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}
