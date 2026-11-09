package ro.cs.tao.persistence;

import ro.cs.tao.component.GroupComponent;

/**
 * Provides an abstraction for managing {@link GroupComponent} entities.
 * The interface extends {@code EntityProvider} to inherit support for common
 * CRUD operations, allowing for operations specific to group components
 * within the system.
 *
 * Implementations of this interface may define additional behaviors and
 * constraints suitable for managing {@link GroupComponent} entities, such
 * as filtering based on visibility or activity status.
 *
 * This interface is particularly useful in systems where {@link GroupComponent}
 * entities represent groupings of other components with hierarchical or
 * aggregated behavior.
 */
public interface GroupComponentProvider extends EntityProvider<GroupComponent, String> {

}
