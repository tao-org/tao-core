package ro.cs.tao.persistence;

import ro.cs.tao.component.ogc.WPSComponent;

/**
 * WPSComponentProvider is an interface for managing and providing operations
 * on entities of type {@code WPSComponent}. It extends the {@code EntityProvider}
 * interface, inheriting generic CRUD operations while promoting a contract for
 * handling {@code WPSComponent} entities identified by a {@code String}.
 *
 * Implementations of this interface are expected to define specific behaviors
 * tailored to the management and persistence of {@code WPSComponent} instances,
 * including validation, saving, updating, and deletion.
 */
public interface WPSComponentProvider extends EntityProvider<WPSComponent, String> {
}
