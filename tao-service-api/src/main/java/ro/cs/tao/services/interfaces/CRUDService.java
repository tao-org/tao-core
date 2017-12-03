package ro.cs.tao.services.interfaces;

import ro.cs.tao.component.validation.ValidationException;

import java.util.List;

/**
 * Interface defining a CRUD entity service
 *
 * @author Cosmin Cara
 */
public interface CRUDService<T> {

    /**
     * Finds an entity by its identifier.
     *
     * @param id    The entity identifier
     * @return      An instance of the entity or <code>null</code> if not found
     */
    T findById(String id);

    /**
     * Retrieves all the entities of this type.
     */
    List<T> list();

    /**
     * Persists a new entity.
     *
     * @param object    The entity to be persisted.
     */
    void save(T object);

    /**
     * Updates an existing entity.
     *
     * @param object    The modified entity.
     */
    void update(T object);

    /**
     * Removes the entity with the given identifier from the underlying persistent storage.
     *
     * @param id    The entity identifier.
     */
    void delete(String id);

    /**
     * Validates the entity field values before persisting it.
     * Implementors should override this method which, by default, does nothing.
     *
     * @param object    The entity to validate.
     * @throws ValidationException  If inconsistent values are found.
     */
    default void validate(T object) throws ValidationException { }
}
