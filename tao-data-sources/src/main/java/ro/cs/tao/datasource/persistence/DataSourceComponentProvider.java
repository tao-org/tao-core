package ro.cs.tao.datasource.persistence;

import ro.cs.tao.Sort;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.persistence.EntityProvider;
import ro.cs.tao.persistence.PersistenceException;

import java.util.List;
import java.util.Set;

/**
 * A provider interface for managing and querying {@link DataSourceComponent} entities. This interface
 * extends {@link EntityProvider} to provide additional methods specific to handling data source components.
 */
public interface DataSourceComponentProvider extends EntityProvider<DataSourceComponent, String> {

    List<DataSourceComponent> list(int pageNumber, int pageSize, Sort sort);
    List<DataSourceComponent> getUserDataSourceComponents(String userId);
    List<DataSourceComponent> getSystemDataSourceComponents();
    List<DataSourceComponent> getSystemDataSourceComponents(int pageNumber, int pageSize, Sort sort);
    long countSystemDataSourceComponents();
    List<DataSourceComponent> getOtherDataSourceComponents(Set<String> ids);
    List<DataSourceComponent> getProductSets(String userId);
    List<DataSourceComponent> getBySourceAndSensor(String dataSourceName, String sensor);
    List<DataSourceComponent> getBySource(String dataSourceName);
    DataSourceComponent getDataSourceComponentByLabel(String label);
    DataSourceComponent getQueryDataSourceComponent(long queryId);
    List<DataSourceComponent> getPRepositoryQueries(String userId);
    List<String> getQueryPathsForRepository(String repositoryId);
    DataSourceComponent getRepositoryQuery(String repositoryId, String path);
    default void deleteRepositoryQuery(String repositoryId, String path) throws PersistenceException {
        DataSourceComponent c = getRepositoryQuery(repositoryId, path);
        if (c != null) {
            delete(c);
        }
    }
}
