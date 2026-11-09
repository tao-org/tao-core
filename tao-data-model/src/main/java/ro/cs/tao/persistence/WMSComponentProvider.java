package ro.cs.tao.persistence;

import ro.cs.tao.component.ogc.WMSComponent;

/**
 * A specialized provider interface for managing {@link WMSComponent} entities.
 * This interface extends {@link EntityProvider} to provide CRUD operations
 * and other entity management functionalities specific to the {@link WMSComponent}
 * type, using a {@link String} identifier.
 *
 * The primary responsibilities include retrieving, saving, updating, and deleting
 * {@link WMSComponent} entities, while enforcing necessary validation or
 * additional constraints during these operations.
 *
 * This interface is designed to be implemented by classes that interact with
 * a persistence layer or an underlying repository to handle {@link WMSComponent}
 * entities.
 */
public interface WMSComponentProvider extends EntityProvider<WMSComponent, String> {
}
