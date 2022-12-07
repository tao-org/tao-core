package ro.cs.tao.datasource;

import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.datasource.remote.FetchMode;

import java.util.Map;

public class DataSourceConfiguration extends StringIdentifiable {
    private FetchMode fetchMode;
    private String localRepositoryPath;
    private Map<String, String> parameters;

    public FetchMode getFetchMode() {
        return fetchMode;
    }

    public void setFetchMode(FetchMode fetchMode) {
        this.fetchMode = fetchMode;
    }

    public String getLocalRepositoryPath() {
        return localRepositoryPath;
    }

    public void setLocalRepositoryPath(String localRepositoryPath) {
        this.localRepositoryPath = localRepositoryPath;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
