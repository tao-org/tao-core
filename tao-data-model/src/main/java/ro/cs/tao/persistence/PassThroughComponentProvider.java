package ro.cs.tao.persistence;

import ro.cs.tao.component.PassThroughComponent;

/**
 * The PassThroughComponentProvider interface extends the {@link EntityProvider} interface
 * and is specialized for handling entities of type {@link PassThroughComponent}
 * with identifiers of type {@link String}.
 *
 * This provider interface facilitates CRUD operations and management of
 * {@link PassThroughComponent} entities, acting as an abstraction layer
 * for their persistence and retrieval.
 */
public interface PassThroughComponentProvider extends EntityProvider<PassThroughComponent, String> {
}
