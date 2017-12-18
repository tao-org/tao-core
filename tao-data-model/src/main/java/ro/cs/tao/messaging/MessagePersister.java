package ro.cs.tao.messaging;

/**
 * @author Cosmin Cara
 */
public interface MessagePersister {

    Message saveMessage(Message message) throws Exception;

}
