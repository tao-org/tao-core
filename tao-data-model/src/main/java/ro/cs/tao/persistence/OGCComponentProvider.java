package ro.cs.tao.persistence;

import ro.cs.tao.component.ogc.OGCProcessComponent;

/**
 * OGCComponentProvider is an interface for managing and providing operations
 * on entities of type {@code OGCProcessComponent}. It extends the {@code EntityProvider}
 * interface, inheriting generic CRUD operations while promoting a contract for
 * handling {@code OGCProcessComponent} entities identified by a {@code String}.
 *
 * Implementations of this interface are expected to define specific behaviors
 * tailored to the management and persistence of {@code OGCProcessComponent} instances,
 * including validation, saving, updating, and deletion.
 */
public interface OGCComponentProvider extends EntityProvider<OGCProcessComponent, String> {
}
