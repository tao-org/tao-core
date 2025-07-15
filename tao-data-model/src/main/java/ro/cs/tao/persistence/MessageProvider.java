package ro.cs.tao.persistence;

import ro.cs.tao.messaging.Message;

import java.util.List;

/**
 * The MessageProvider interface defines the contract for handling operations related to Message entities.
 * It extends the {@link EntityProvider} interface, providing additional methods to manage user-specific messages.
 * Implementations are expected to handle persistence, retrieval, and management of message data.
 */
public interface MessageProvider extends EntityProvider<Message, Long> {

    /**
     * Retrieves a paginated list of messages for the specified user.
     *
     * @param userId The unique identifier of the user whose messages are to be retrieved.
     * @param pageNumber The page number to retrieve, used for pagination of the message results.
     * @return A list of {@code Message} objects associated with the specified user, corresponding to the given page.
     */
    List<Message> getUserMessages(String userId, Integer pageNumber);
    /**
     * Retrieves a list of unread messages for a specific user.
     *
     * @param userId The unique identifier of the user whose unread messages are to be retrieved.
     * @return A list of {@code Message} objects that are marked as unread for the specified user.
     */
    List<Message> getUnreadMessages(String userId);
    /**
     * Retrieves a specific message for a given user based on the provided timestamp.
     *
     * @param userId The unique identifier of the user to whom the message belongs.
     * @param timestamp The timestamp of the message to retrieve, representing the time it was created or logged.
     * @return The {@code Message} object corresponding to the given user and timestamp, or {@code null} if no matching message is found.
     */
    Message get(String userId, long timestamp);
    /**
     * Acknowledges the specified messages for the given user. This method marks the messages
     * identified by the provided list of message IDs as acknowledged for the specified user.
     *
     * @param messageIds A list of unique identifiers representing the messages to be acknowledged.
     * @param userId The unique identifier of the user for whom the messages are being acknowledged.
     */
    void acknowledge(List<Long> messageIds, String userId);
    /**
     * Removes all messages associated with the specified user.
     *
     * @param userId The unique identifier of the user whose messages are to be deleted.
     */
    void clear(String userId);

}
