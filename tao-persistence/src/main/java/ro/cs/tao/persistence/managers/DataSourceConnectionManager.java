/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package ro.cs.tao.persistence.managers;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.datasource.DataSourceCredentials;
import ro.cs.tao.persistence.DataSourceCredentialsProvider;
import ro.cs.tao.persistence.repository.DataSourceConnectionRepository;

import java.util.List;
@Configuration
@EnableTransactionManagement
@Component("dataSourceConnectionManager")
public class DataSourceConnectionManager
        extends EntityManager<DataSourceCredentials, Long, DataSourceConnectionRepository>
        implements DataSourceCredentialsProvider {

    @Override
    public List<DataSourceCredentials> getByUser(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public DataSourceCredentials get(String userId, String dataSource) {
        return repository.findByUserIdAndDataSource(userId, dataSource);
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkId(Long entityId, boolean existingEntity) {
        return entityId != null && entityId >= 0;
    }

    @Override
    protected boolean checkEntity(DataSourceCredentials entity) {
        return true;
    }
}
