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

import org.springframework.stereotype.Component;
import ro.cs.tao.execution.model.DataSourceConnection;
import ro.cs.tao.persistence.repository.DataSourceConnectionRepository;

import java.util.List;

@Component("dataSourceConnectionManager")
public class DataSourceConnectionManager extends EntityManager<DataSourceConnection, Long, DataSourceConnectionRepository> {

    public List<DataSourceConnection> get(int userId) {
        return repository.findByUserId(userId);
    }

    public DataSourceConnection get(int userId, String dataSource) {
        return repository.findByUserIdAndDataSource(userId, dataSource);
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkId(Long entityId, boolean existingEntity) {
        return (!existingEntity && (entityId == null || entityId == 0)) ||
                (existingEntity && entityId != null && entityId > 0);
    }

    @Override
    protected boolean checkEntity(DataSourceConnection entity) {
        return true;
    }
}
