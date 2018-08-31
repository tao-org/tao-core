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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.execution.model.Query;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.QueryRepository;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("queryManager")
public class QueryManager {

    private Logger logger = Logger.getLogger(QueryManager.class.getName());

    /** CRUD Repository for Query entities */
    @Autowired
    private QueryRepository queryRepository;

    public Query findById(long id) {
        final Optional<Query> query = queryRepository.findById(id);
        return query.orElse(null);
    }

    public Query findByUserIdAndSensorAndDataSourceAndWorkflowNodeId(String userId, String sensor, String dataSource, long nodeId) {
        return queryRepository.findByUserIdAndSensorAndDataSourceAndWorkflowNodeId(userId, sensor, dataSource, nodeId);
    }

    public List<Query> findByUserIdAndWorkflowNodeId(String userId, long nodeId) {
        return queryRepository.findByUserIdAndWorkflowNodeId(userId, nodeId);
    }

    public List<Query> findByUserIdAndSensorAndDataSource(String userId, String sensor, String dataSource) {
        return queryRepository.findByUserIdAndSensorAndDataSource(userId, sensor, dataSource);
    }

    public List<Query> findByUserId(String userId) {
        return queryRepository.findByUserId(userId);
    }

    public List<Query> findByUserIdAndSensor(String userId, String sensor) {
        return queryRepository.findByUserIdAndSensor(userId, sensor);
    }

    public List<Query> findByUserIdAndDataSource(String userId, String dataSource) {
        return queryRepository.findByUserIdAndDataSource(userId, dataSource);
    }

    public Page<Query> findAll(Pageable pageable) {
        return queryRepository.findAll(pageable);
    }

    @Transactional
    public Query saveQuery(Query query) throws PersistenceException {
        // check method parameters
        if(!checkQuery(query)) {
            throw new PersistenceException("Invalid parameters were provided for adding new query !");
        }

        // save the new Query entity and return it
        return queryRepository.save(query);
    }

    public void removeQuery(Query query) {
        queryRepository.delete(query);
    }

    private boolean checkQuery(Query query) {
        return query != null && query.getUserId() != null && query.getWorkflowNodeId() > 0 &&
                query.getSensor() != null && query.getDataSource() != null;
    }
}
