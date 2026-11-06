package ro.cs.tao.persistence;

import ro.cs.tao.Sort;
import ro.cs.tao.Tag;
import ro.cs.tao.component.Identifiable;

import java.util.List;

/**
 * Provides an abstraction layer for managing entities that implement the {@link Identifiable} interface.
 * This interface defines generic methods for CRUD operations such as retrieving, saving, updating,
 * deleting, and checking the existence of entities. The {@code EntityProvider} can be implemented
 * to provide specific behavior based on the type of entity and its identifier.
 *
 * @param <E> The type of the entity, which must implement {@link Identifiable}.
 * @param <K> The type of the identifier used by the entity.
 */
public interface EntityProvider<E extends Identifiable<K>, K> {

    /**
     * Retrieves a list of all entities managed by the provider.
     *
     * @return a list of entities of type E.
     */
    List<E> list();

    /**
     * Retrieves a list of entities matching the given identifiers.
     *
     * @param identifiers an iterable collection of identifiers of type K
     *                    for which the corresponding entities are to be retrieved
     * @return a list of entities of type E that match the provided identifiers
     */
    List<E> list(Iterable<K> identifiers);

    /**
     * Retrieves a paginated and sorted list of entities managed by the provider.
     *
     * @param pageNumber the page number to retrieve, starting from 0
     * @param pageSize the number of entities to be included in a single page
     * @param sort the sorting criteria to be applied to the list of entities
     * @return a list of entities of type E for the specified page and sorting criteria
     */
    List<E> list(int pageNumber, int pageSize, Sort sort);

    default List<E> list(String label, int pageNumber, int pageSize, Sort sort) { return list(pageNumber, pageSize, sort); }

    default List<E> list(List<Tag> tags, int pageNumber, int pageSize, Sort sort) { return list(pageNumber, pageSize, sort); }

    default List<E> list(String label, List<Tag> tags, int pageNumber, int pageSize, Sort sort) { return list(pageNumber, pageSize, sort); }

    default long count() { return list().size(); }

    default long count(String label, List<Tag> tags) { return count(); }

    /**
     * Retrieves an entity of type E using the specified identifier.
     *
     * @param id the identifier of type K used to retrieve the entity
     * @return the entity of type E corresponding to the provided identifier, or null if no such entity exists
     */
    E get(K id);

    /**
     * Saves the provided entity in the persistence layer. If the entity does not already exist,
     * it will be created. If the entity exists, it will be updated.
     *
     * @param entity the entity of type E to be saved
     * @return the saved entity of type E
     * @throws PersistenceException if an error occurs during the save operation
     */
    E save(E entity) throws PersistenceException;

    /**
     * Updates the provided entity in the persistence layer. If the entity does not already exist,
     * it will be created. Otherwise, it will be updated.
     *
     * @param entity the entity of type E to be updated
     * @return the updated entity of type E
     * @throws PersistenceException if an error occurs during the update operation
     */
    default E update(E entity) throws PersistenceException { return save(entity); }

    /**
     * Checks if an entity with the specified identifier exists in the persistence layer.
     *
     * @param id the identifier of type K to check for existence
     * @return true if an entity with the specified identifier exists, false otherwise
     */
    default boolean exists(K id) { return get(id) != null; }

    /**
     * Deletes an entity identified by the given identifier from the persistence layer.
     *
     * @param id the identifier of type K corresponding to the entity to be deleted
     * @throws PersistenceException if an error occurs during the deletion process
     */
    void delete (K id) throws PersistenceException;

    /**
     * Deletes the specified entity from the persistence layer.
     * If the entity is not null, the deletion operation will be performed using the entity's identifier.
     *
     * @param entity the entity of type E to be deleted
     * @throws PersistenceException if an error occurs during the deletion process
     */
    default void delete(E entity) throws PersistenceException {
        if (entity != null) {
            delete(entity.getId());
        }
    }

    /**
     * Deletes entities identified by the given collection of identifiers from the persistence layer.
     *
     * @param ids an iterable collection of identifiers of type K corresponding to the entities to be deleted
     * @throws PersistenceException if an error occurs during the deletion process
     */
    void delete(Iterable<K> ids) throws PersistenceException;
}
