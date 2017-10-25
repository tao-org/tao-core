package ro.cs.tao;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.beans.Transient;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "message")
public class Message {
    private long timestamp;
    private Object source;
    private String data;

    public Message() { }

    public Message(long timestamp, Object source, String data) {
        this.timestamp = timestamp;
        this.source = source;
        this.data = data;
    }

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
