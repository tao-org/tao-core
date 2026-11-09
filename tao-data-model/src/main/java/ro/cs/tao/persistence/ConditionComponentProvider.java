package ro.cs.tao.persistence;

import ro.cs.tao.component.ConditionComponent;

/**
 * Provides an interface for managing entities of type {@link ConditionComponent} with identifiers of type {@link String}.
 * This interface extends the {@link EntityProvider} and inherits its methods for common CRUD operations.
 * It serves as a specialized contract for handling {@link ConditionComponent} entities with additional or overridden functionality.
 */
public interface ConditionComponentProvider extends EntityProvider<ConditionComponent, String> {
}
