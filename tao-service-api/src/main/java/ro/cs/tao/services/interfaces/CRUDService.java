package ro.cs.tao.services.interfaces;

import ro.cs.tao.component.validation.ValidationException;

import java.util.List;

/**
 * Interface defining a CRUD entity service
 *
 * @author Cosmin Cara
 */
public interface CRUDService<T> {

    T findById(String id);

    List<T> list();

    void save(T object);

    void update(T object);

    void delete(String id);

    default void validate(T object) throws ValidationException { }
}
