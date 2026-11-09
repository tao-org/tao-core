package ro.cs.tao.persistence;

import ro.cs.tao.datasource.DataSourceCredentials;

import java.util.List;

/**
 * Provides an abstraction for managing and retrieving {@link DataSourceCredentials} entities.
 * This interface extends the {@code EntityProvider} and offers additional methods
 * specific to handling credentials for data sources linked to a user.
 *
 * The {@code DataSourceCredentialsProvider} enables querying credentials
 * by user-specific filters and supports retrieving credentials for a specific
 * combination of user and data source.
 *
 * @see DataSourceCredentials
 * @see EntityProvider
 */
public interface DataSourceCredentialsProvider extends EntityProvider<DataSourceCredentials, Long> {

    /**
     * Retrieves a list of {@link DataSourceCredentials} associated with a specific user.
     *
     * @param user the identifier for the user whose data source credentials are to be retrieved
     * @return a list of {@link DataSourceCredentials} instances associated with the given user;
     *         an empty list if no credentials are found for the user
     */
    List<DataSourceCredentials> getByUser(String user);
    /**
     * Retrieves the {@link DataSourceCredentials} associated with the specified user and data source.
     *
     * @param user the identifier for the user whose data source credentials are being retrieved
     * @param dataSourceId the identifier for the specific data source
     * @return the {@link DataSourceCredentials} instance matching the given user and data source;
     *         or null if no matching credentials are found
     */
    DataSourceCredentials get(String user, String dataSourceId);
}
