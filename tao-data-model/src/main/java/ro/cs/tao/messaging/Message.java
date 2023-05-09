/*
 * Copyright (C) 2018 CS ROMANIA
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

import ro.cs.tao.component.LongIdentifiable;
import ro.cs.tao.serialization.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.beans.Transient;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for messages that circulate via the available {@link EventBus}.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "message")
public class Message extends LongIdentifiable {
    public static final String PRINCIPAL_KEY = "Principal";
    public static final String PAYLOAD_KEY = "Payload";
    public static final String SOURCE_KEY = "Source";
    public static final String TOPIC_KEY = "Topic";
    public static final String MESSAGE_KEY = "Message";
    private static Serializer<Message, String> serializer;
    private static MapAdapter mapAdapter;
    private long timestamp;
    private boolean read;
    private boolean persistent;
    private Map<String, String> data;

    static {
        try {
            serializer = SerializerFactory.create(Message.class, MediaType.JSON);
            mapAdapter = new MapAdapter();
        } catch (SerializationException e) {
            e.printStackTrace();
        }
    }

    public static Message create(String user, Object source, String message) {
        return create(user, source, message, true);
    }

    public static Message create(String user, Object source, String message, boolean persistent) {
        HashMap<String, String> data = new HashMap<>();
        data.put(PRINCIPAL_KEY, user);
        data.put(MESSAGE_KEY, message);
        if (source != null) {
            data.put(SOURCE_KEY, source.toString());
        }
        return new Message(System.currentTimeMillis(), data, persistent);
    }

    public static Message create(String user, Object source, String message, String additional, boolean persistent) {
        HashMap<String, String> data = new HashMap<>();
        data.put(PRINCIPAL_KEY, user);
        data.put(MESSAGE_KEY, message);
        data.put(PAYLOAD_KEY, additional);
        if (source != null) {
            data.put(SOURCE_KEY, source.toString());
        }
        return new Message(System.currentTimeMillis(), data, persistent);
    }

    public Message() { super(); }

    private Message(long timestamp, HashMap<String, String> data) {
        this(timestamp, data, true);
    }
    private Message(long timestamp, HashMap<String, String> data, boolean persistent) {
        this.id = 0L;
        this.timestamp = timestamp;
        this.data = data;
        this.read = false;
        this.persistent = persistent;
    }

    public String getTopic() { return this.data != null ? this.data.get(TOPIC_KEY) : null; }
    public void setTopic(String value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(TOPIC_KEY, value);
    }

    @Transient
    public String getMessage() { return this.data != null ? this.data.get(MESSAGE_KEY) : null; }
    public void setMessage(String value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(MESSAGE_KEY, value);
    }

    @XmlElement(name = "user")
    public String getUser() { return this.data != null ? this.data.get(PRINCIPAL_KEY) : null; }
    public void setUser(String name) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(PRINCIPAL_KEY, name);
    }

    @XmlElement(name = "isRead")
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    @XmlElement(name = "timestamp")
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @XmlElement(name = "data")
    public String getData() {
        try {
            return this.data != null ? mapAdapter.unmarshal(this.data) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public void setData(String data) {
        try {
            this.data = mapAdapter.marshal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transient
    public String getPayload() {
        return this.data != null ? this.data.get(PAYLOAD_KEY) : null;
    }
    public void setPayload(String data) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(PAYLOAD_KEY, data);
    }

    @Transient
    public boolean isPersistent() { return persistent; }
    public void setPersistent(boolean persistent) { this.persistent = persistent; }

    @Transient
    public String getItem(String key) {
        return data != null ? data.get(key) : null;
    }
    public void addItem(String key, String value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
    }

    @Transient
    public Map<String, String> getItems() { return data; }

    @Override
    public String toString() {
        try {
            return serializer != null ? serializer.serialize(this) : super.toString();
        } catch (SerializationException e) {
            return super.toString();
        }
    }
}
