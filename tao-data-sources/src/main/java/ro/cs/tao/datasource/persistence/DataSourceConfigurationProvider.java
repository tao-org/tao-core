package ro.cs.tao.datasource.persistence;

import ro.cs.tao.datasource.DataSourceConfiguration;
import ro.cs.tao.persistence.EntityProvider;

/**
 * Provides an interface for managing and interacting with {@link DataSourceConfiguration}
 * entities. Extends {@link EntityProvider} to include generic methods for handling
 * CRUD operations such as retrieving, saving, updating, and deleting data source configurations.
 *
 * The primary purpose of this interface is to act as a contract for implementations
 * responsible for managing {@link DataSourceConfiguration} objects, which represent
 * the configuration details of data sources with string-based identifiers.
 *
 * This interface can be implemented to define custom logic for persisting and managing
 * {@link DataSourceConfiguration} entities, including validation and additional operations.
 *
 * @see EntityProvider
 * @see DataSourceConfiguration
 */
public interface DataSourceConfigurationProvider extends EntityProvider<DataSourceConfiguration, String> {
}
