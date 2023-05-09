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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.datasource.persistence.QueryProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.repository.QueryRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("queryManager")
public class QueryManager extends EntityManager<Query, Long, QueryRepository> implements QueryProvider {

    @Override
    public Query get(String userId, String label) {
        return repository.findByUserIdAndLabel(userId, label);
    }

    @Override
    public List<Query> list(String userId) { return repository.getUserQueries(userId); }

    @Override
    public Query get(String userId, String sensor, String dataSource, long nodeId) {
        return repository.getQuery(userId, sensor, dataSource, nodeId);
    }

    @Override
    public List<Query> list(String userId, long nodeId) {
        return repository.getUserQueries(userId, nodeId);
    }

    @Override
    public List<Query> listBySensor(String userId, String sensor) {
        return repository.getQueriesByUserAndSensor(userId, sensor);
    }

    @Override
    public List<Query> listByDataSource(String userId, String dataSource) {
        return repository.getQueriesByUserAndDataSource(userId, dataSource);
    }

    @Override
    public List<Query> list(String userId, String sensor, String dataSource) {
        return repository.getUserQueries(userId, sensor, dataSource);
    }

    @Override
    public Query save(Query query) throws PersistenceException {
        LocalDateTime timestamp = LocalDateTime.now();
        final Query existingQuery;
        if (query.getLabel() == null) {
            query.setLabel(String.format("Query for %s on %s - %s",
                                         query.getSensor(), query.getDataSource(),
                                         timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            existingQuery = get(query.getUserId(), query.getSensor(), query.getDataSource(), query.getWorkflowNodeId());
        } else {
            existingQuery = get(query.getUserId(), query.getLabel());
        }
        try {
            if (existingQuery != null && query.getId() != null && !existingQuery.getId().equals(query.getId())) {
                throw new PersistenceException(String.format("There is already a query with the same label for the user %s",
                                                             query.getUserId()));
            }
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
        if (query.getCreated() == null) {
            query.setCreated(timestamp);
        }
        if (query.getWorkflowNodeId() != null && query.getWorkflowNodeId() == 0) {
            query.setWorkflowNodeId(null);
        }
        query.setModified(timestamp);
        if (query.getId() == null) {
            if (existingQuery != null) {
                query.setId(existingQuery.getId());
                return super.update(query);
            } else {
                return super.save(query);
            }
        } else {
            return super.update(query);
        }
    }

    @Override
    protected String identifier() {return "id"; }

    @Override
    protected boolean checkEntity(Query query) {
        return query != null && query.getUserId() != null &&
                query.getSensor() != null && query.getDataSource() != null;
    }
}
