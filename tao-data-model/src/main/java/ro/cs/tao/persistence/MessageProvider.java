package ro.cs.tao.persistence;

import ro.cs.tao.messaging.Message;

import java.util.List;

public interface MessageProvider extends EntityProvider<Message, Long> {

    List<Message> getUserMessages(String user, Integer pageNumber);
    List<Message> getUnreadMessages(String user);

}
