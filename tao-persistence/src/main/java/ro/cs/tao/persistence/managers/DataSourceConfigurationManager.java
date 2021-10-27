package ro.cs.tao.persistence.managers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ro.cs.tao.datasource.DataSourceConfiguration;
import ro.cs.tao.datasource.persistence.DataSourceConfigurationProvider;
import ro.cs.tao.persistence.repository.DataSourceConfigurationRepository;

@Component("dataSourceConfigurationManager")
public class DataSourceConfigurationManager
        extends EntityManager<DataSourceConfiguration, String, DataSourceConfigurationRepository>
        implements DataSourceConfigurationProvider {
    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(DataSourceConfiguration entity) {
        return StringUtils.isNotEmpty(entity.getId()) && entity.getFetchMode() != null;
    }
}
