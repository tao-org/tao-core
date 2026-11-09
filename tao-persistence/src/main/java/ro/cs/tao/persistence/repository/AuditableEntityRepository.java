package ro.cs.tao.persistence.repository;

import org.hibernate.internal.SessionImpl;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * A repository interface for managing entities that are auditable. Provides an additional method
 * to update the "modified" timestamp of an entity in a non-blocking manner using the specified ID.
 *
 * This interface extends {@link PagingAndSortingRepository}, enabling CRUD operations
 * while supporting pagination and sorting.
 *
 * @param <T>  the type of the entity
 * @param <ID> the type of the entity's identifier
 */
@NoRepositoryBean
public interface AuditableEntityRepository<T, ID> extends OptimisedPagingAndSortingRepository<T, ID> {

    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final String UPDATE_MODIFIED_QUERY = "UPDATE %s SET modified = now() WHERE id = ?";

    default void updateModified(T entity, ID id) throws PersistenceException {
        final EntityManager entityManager = SpringContextBridge.services()
                                                               .getService(EntityManager.class);
        final SessionImpl session = entityManager.unwrap(SessionImpl.class);
        AbstractEntityPersister persister = (AbstractEntityPersister) session.getEntityPersister(null, entity);
        String tableName = persister.getTableName();
        // The synchronous execution would trigger the session to flush, which, in turn, re-triggers the update listener
        executor.execute(() -> {
            try {
                final Query query = entityManager.createNativeQuery(String.format(UPDATE_MODIFIED_QUERY, tableName))
                                                 .setParameter(1, id);
                query.executeUpdate();
            } catch (Exception e) {
                Logger.getLogger(AuditableEntityRepository.class.getName()).warning(e.getMessage());
            }
        });
    }

}
