package ro.cs.tao.datasource;

import ro.cs.tao.persistence.PersistenceException;

import java.util.List;

public interface DownloadQueuePersister {
    DownloadQueueItem save(DownloadQueueItem item) throws PersistenceException;
    DownloadQueueItem load(String id);
    void remove(String id);
    List<DownloadQueueItem> restore();
}
