package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.datasource.DownloadQueueItem;

@Repository
@Qualifier(value = "downloadQueueRepository")
@Transactional
public interface DownloadQueueRepository extends JpaRepository<DownloadQueueItem, String> {
}

