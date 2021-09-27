package ro.cs.tao.persistence;

import ro.cs.tao.datasource.DataSourceCredentials;

import java.util.List;

public interface DataSourceCredentialsProvider extends EntityProvider<DataSourceCredentials, Long> {

    List<DataSourceCredentials> getByUser(String user);
    DataSourceCredentials get(String user, String dataSourceId);
}
