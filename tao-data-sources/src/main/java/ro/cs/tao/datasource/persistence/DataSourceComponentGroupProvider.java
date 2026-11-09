package ro.cs.tao.datasource.persistence;

import ro.cs.tao.datasource.DataSourceComponentGroup;
import ro.cs.tao.persistence.EntityProvider;

import java.util.List;

/**
 * Provides methods for managing {@link DataSourceComponentGroup} entities.
 * Extends the {@link EntityProvider} interface to include CRUD operations
 * and additional methods specific to handling groups of data source components.
 *
 * A DataSourceComponentGroup groups multiple data source components,
 * typically created when constructing datasets consisting of multiple
 * queries or product types.
 *
 * This interface defines additional methods for querying and retrieving
 * {@link DataSourceComponentGroup} instances based on user and label
 * information.
 *
 * @param <DataSourceComponentGroup>
 *        The type of entity to be managed, representing a group of data source components.
 * @param <String>
 *        The type of the entity's identifier.
 */
public interface DataSourceComponentGroupProvider extends EntityProvider<DataSourceComponentGroup, String> {

    List<DataSourceComponentGroup> listByUser(String userName);
    DataSourceComponentGroup getDataSourceComponentGroupByLabel(String label);

}
