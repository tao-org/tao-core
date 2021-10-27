package ro.cs.tao.datasource;

import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.datasource.remote.FetchMode;

public class DataSourceConfiguration extends StringIdentifiable {
    private FetchMode fetchMode;
    private String localRepositoryPath;

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
}
