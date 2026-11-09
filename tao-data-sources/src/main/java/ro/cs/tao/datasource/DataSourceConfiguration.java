package ro.cs.tao.datasource;

import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.datasource.remote.FetchMode;

import java.util.Map;

/**
 * Configuration class for data sources, inheriting string-based identification capabilities
 * from the {@link StringIdentifiable} class. This class encapsulates the configuration
 * parameters required for setting up and managing a data source.
 *
 * It defines the operational mode, repository path, and additional properties
 * needed to complement the data source's functionality.
 */
public class DataSourceConfiguration extends StringIdentifiable {
    private FetchMode fetchMode;
    private String localRepositoryPath;
    private Map<String, String> parameters;

    /**
     * Retrieves the current fetch mode configuration for the data source.
     *
     * @return the fetch mode, which determines the behavior of product retrieval (e.g., overwrite, resume, copy, symlink, or check).
     */
    public FetchMode getFetchMode() {
        return fetchMode;
    }

    /**
     * Sets the fetch mode for the data source configuration, which determines
     * the behavior of product retrieval (e.g., overwrite, resume, copy, symlink, or check).
     *
     * @param fetchMode the fetch mode to be set
     */
    public void setFetchMode(FetchMode fetchMode) {
        this.fetchMode = fetchMode;
    }

    /**
     * Retrieves the path to the local repository associated with the data source.
     *
     * @return the local repository path as a string
     */
    public String getLocalRepositoryPath() {
        return localRepositoryPath;
    }

    /**
     * Sets the local repository path for the data source configuration.
     * The local repository path specifies the directory where local data
     * related to the data source will be stored.
     *
     * @param localRepositoryPath the path to the local repository as a string
     */
    public void setLocalRepositoryPath(String localRepositoryPath) {
        this.localRepositoryPath = localRepositoryPath;
    }

    /**
     * Retrieves the configuration parameters for the data source.
     *
     * @return a map containing key-value pairs of parameters for the data source configuration
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets the configuration parameters for the data source.
     * The parameters are specified as a map containing key-value pairs,
     * where each key represents a parameter name and the corresponding value
     * represents the parameter value.
     *
     * @param parameters a map of configuration parameters, where keys are strings
     *                   representing parameter names and values are strings
     *                   representing parameter values
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
