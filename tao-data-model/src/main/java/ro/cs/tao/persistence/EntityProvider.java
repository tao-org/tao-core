package ro.cs.tao.persistence;

import ro.cs.tao.Sort;
import ro.cs.tao.component.Identifiable;

import java.util.List;

public interface EntityProvider<E extends Identifiable<K>, K> {

    List<E> list();
    List<E> list(Iterable<K> identifiers);
    List<E> list(int pageNumber, int pageSize, Sort sort);
    E get(K id);
    E save(E entity) throws PersistenceException;
    default E update(E entity) throws PersistenceException { return save(entity); }
    default boolean exists(K id) { return get(id) != null; }
    void delete (K id) throws PersistenceException;
    default void delete(E entity) throws PersistenceException {
        if (entity != null) {
            delete(entity.getId());
        }
    }
    void delete(Iterable<K> ids) throws PersistenceException;
}
