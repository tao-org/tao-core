package ro.cs.tao.persistence;

import ro.cs.tao.messaging.Message;

import java.util.List;

public interface MessageProvider extends EntityProvider<Message, Long> {

    List<Message> getUserMessages(String userId, Integer pageNumber);
    List<Message> getUnreadMessages(String userId);
    Message get(String userId, long timestamp);
    void acknowledge(List<Long> messageIds, String userId);
    void clear(String userId);

}
