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

package ro.cs.tao.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.cs.tao.execution.model.Query;

import java.util.List;

public interface QueryService extends CRUDService<Query, Long> {

    Query getQueryById(long id);
    Query getQuery(String userId, String sensor, String dataSource, long workflowNodeId);
    List<Query> getQueries(String userId, String sensor, String dataSource);
    List<Query> getQueries(String userId);
    List<Query> getQueries(String userId, long nodeid);
    List<Query> getQueriesBySensor(String userId, String sensor);
    List<Query> getQueriesByDataSource(String userId, String dataSource);
    Page<Query> getAllQueries(Pageable pageable);
}
