package ro.cs.tao.persistence.audit;

import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.managers.SimpleCache;
import ro.cs.tao.persistence.managers.TaoComponentManager;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.utils.AutoEvictableCache;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * The TaoComponentUpdateListener class is designed to provide lifecycle handling
 * for entity update events such as persistence, modification, or deletion.
 * It ensures that updates to entity objects are reflected in caching mechanisms
 * and notifies associated managers of lifecycle changes.
 *
 * This abstract class serves as a base for specialized listeners tailored
 * to specific entity types, with the following primary responsibilities:
 *
 * - Adding or updating entities in the cache upon persistence or updates.
 * - Removing entities from the cache upon deletion.
 * - Notifying associated managers whenever an entity's state is modified.
 *
 * Key Methods:
 * - `onAfterUpdate`: Called after an entity is updated or persisted, ensuring
 *   the entity is added to or updated in the cache and notifying any associated
 *   component managers about the change.
 * - `onAfterDelete`: Called after an entity is deleted, ensuring the entity
 *   is removed from the cache.
 * - `getManager`: Retrieves the appropriate manager associated with the given
 *   entity to handle its state changes.
 *
 * Subclasses of TaoComponentUpdateListener typically provide concrete implementations
 * for specific entity types and may use annotations provided by entity lifecycle
 * management frameworks to trigger these methods automatically.
 */
public class TaoComponentUpdateListener {

    static AutoEvictableCache<String, LocalDateTime> lastUpdatedMap = new AutoEvictableCache<>(new Function<String, LocalDateTime>() {
        @Override
        public LocalDateTime apply(String s) {
            return LocalDateTime.now();
        }
    }, 5);

    /**
     * Handles caching and associated manager updates after an entity is persisted or updated.
     * This method is automatically invoked after the entity is saved or modified to ensure that
     * the entity is added to or updated in the cache and that any associated managers are notified
     * of its modification.
     *
     * @param component the entity that has been persisted or updated. This entity is used to update
     *                  the cache with its current state and notify the corresponding component manager.
     */
    @PostPersist
    @PostUpdate
    public void onAfterUpdate(TaoComponent component) {
        final SimpleCache.Cache cache = SimpleCache.getCache(component.getClass());
        if (cache != null) {
            cache.put(component.getId(), component);
        }
        LocalDateTime lastUpdated = lastUpdatedMap.get(component.getId());
        if (lastUpdated == null || Duration.between(lastUpdated, LocalDateTime.now()).toSeconds() > 2) {
            final TaoComponentManager manager = getManager(component);
            if (manager != null) {
                try {
                    lastUpdatedMap.put(component.getId(), LocalDateTime.now());
                    manager.updateModified(component);
                } catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Handles the removal of an entity from the cache after it is deleted.
     * This method is automatically invoked after the entity is removed from
     * the persistent storage to ensure that any cached instance of the entity
     * is also invalidated.
     *
     * @param component the entity that has been deleted. This entity is used
     *                  to identify and remove its corresponding cached entry.
     */
    @PostRemove
    public void onAfterDelete(TaoComponent component) {
        final SimpleCache.Cache cache = SimpleCache.getCache(component.getClass());
        if (cache != null) {
            cache.remove(component.getId());
        }
    }


    private TaoComponentManager getManager(TaoComponent component) {
        final PersistenceManager persistenceManager = SpringContextBridge.services().getService(PersistenceManager.class);
        return persistenceManager.managerFor(component);
    }
}
