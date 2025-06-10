package ro.cs.tao.datasource;

import ro.cs.tao.persistence.PersistenceException;

import java.util.List;

/**
 * Interface for managing the persistence and retrieval of {@link DownloadQueueItem} objects.
 * Provides methods to save, load, remove, and restore download queue items.
 */
public interface DownloadQueuePersister {
    /**
     * Saves the provided {@link DownloadQueueItem} into persistent storage.
     * If the item already exists, it may be updated, otherwise it will be created.
     *
     * @param item the {@link DownloadQueueItem} to be saved, containing all necessary information
     *             about the download queue item to be persisted
     * @return the saved {@link DownloadQueueItem} instance, potentially updated with additional metadata
     * @throws PersistenceException if an error occurs while attempting to save the item
     */
    DownloadQueueItem save(DownloadQueueItem item) throws PersistenceException;

    /**
     * Loads a {@link DownloadQueueItem} from persistent storage using the specified identifier.
     *
     * @param id the unique identifier of the {@link DownloadQueueItem} to be retrieved from storage
     * @return the retrieved {@link DownloadQueueItem} instance, or null if no item with the given identifier exists
     */
    DownloadQueueItem load(String id);

    /**
     * Removes the {@link DownloadQueueItem} identified by the provided unique identifier
     * from persistent storage. If no item with the given identifier exists, the method
     * performs no action.
     *
     * @param id the unique identifier of the {@link DownloadQueueItem} to be removed
     */
    void remove(String id);

    /**
     * Restores all persisted {@link DownloadQueueItem} objects from storage.
     * This method retrieves the currently stored download queue items and returns them
     * as a list, allowing the queue to be reloaded to its previous state.
     *
     * @return a list of {@link DownloadQueueItem} objects retrieved from persistent storage,
     * or an empty list if no items are found.
     */
    List<DownloadQueueItem> restore();
}
