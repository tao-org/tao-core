package ro.cs.tao.persistence.repository;

import jakarta.persistence.EntityManager;
import org.springframework.data.repository.NoRepositoryBean;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;

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
    final String UPDATE_MODIFIED_QUERY = "UPDATE %s e SET e.modified = CURRENT_TIMESTAMP WHERE e.id = :id";

    default void updateModified(T entity, ID id) throws PersistenceException {
        final EntityManager entityManager = SpringContextBridge.services()
                                                               .getService(EntityManager.class);
        final String entityName = entityManager.getMetamodel().entity(entity.getClass()).getName();
        // The synchronous execution would trigger the session to flush, which, in turn, re-triggers the update listener
        executor.execute(() -> {
            try {
                final jakarta.persistence.Query query = entityManager.createQuery(String.format(UPDATE_MODIFIED_QUERY, entityName))
                                                 .setParameter("id", id);
                query.executeUpdate();
            } catch (Exception e) {
                Logger.getLogger(AuditableEntityRepository.class.getName()).warning(e.getMessage());
            }
        });
    }

}



