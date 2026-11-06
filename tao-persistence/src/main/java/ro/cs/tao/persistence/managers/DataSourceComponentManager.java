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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.SortDirection;
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.persistence.DataSourceComponentProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.repository.DataSourceComponentRepository;
import ro.cs.tao.persistence.repository.SourceDescriptorRepository;
import ro.cs.tao.persistence.repository.TargetDescriptorRepository;
import ro.cs.tao.utils.StringUtilities;

import java.util.*;

@Configuration
@EnableTransactionManagement
@Component("dataSourceComponentManager")
public class DataSourceComponentManager extends TaoComponentManager<DataSourceComponent, DataSourceComponentRepository>
                                        implements DataSourceComponentProvider {

    @Autowired
    private SourceDescriptorRepository sourceDescriptorRepository;
    @Autowired
    private TargetDescriptorRepository targetDescriptorRepository;

    @Override
    public List<DataSourceComponent> getUserDataSourceComponents(String userId) {
        return userId != null ? this.repository.getUserDataSourceComponents(userId) :
               this.repository.getUserDataSourceComponents();
    }

    @Override
    public List<DataSourceComponent> getSystemDataSourceComponents() {
        return this.repository.getSystemDataSourceComponents();
    }

    @Override
    public List<DataSourceComponent> getSystemDataSourceComponents(int pageNumber, int pageSize, ro.cs.tao.Sort sort) {
        if (pageNumber < 1 || pageSize < 1) {
            return List.of();
        }
        Sort jpaSort;
        if (sort != null && sort.getFieldsForSort() != null && !sort.getFieldsForSort().isEmpty()) {
            List<Sort.Order> orders = new ArrayList<>();
            for (Map.Entry<String, SortDirection> entry : sort.getFieldsForSort().entrySet()) {
                orders.add(new Sort.Order(SortDirection.ASC.equals(entry.getValue()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                                          entry.getKey()));
            }
            jpaSort = Sort.by(orders);
        } else {
            jpaSort = Sort.by(Sort.Direction.ASC, identifier());
        }
        Page<DataSourceComponent> page = this.repository.getSystemDataSourceComponents(PageRequest.of(pageNumber - 1, pageSize, jpaSort));
        return page.hasContent() ? page.getContent() : List.of();
    }

    @Override
    public long countSystemDataSourceComponents() {
        return this.repository.countSystemDataSourceComponents();
    }

    @Override
    public List<DataSourceComponent> getOtherDataSourceComponents(Set<String> ids) {
        return this.repository.getOtherDataSourceComponents(ids);
    }

    @Override
    public DataSourceComponent getDataSourceComponentByLabel(String label) {
        List<DataSourceComponent> components = this.repository.getDataSourceComponentByLabel(label);
        return (components == null || components.isEmpty()) ? null : components.get(0);
    }

    @Override
    public List<DataSourceComponent> getProductSets(String userId) {
        List<DataSourceComponent> components = this.repository.getUserDataSourceComponents(userId);
        if (components != null) {
            components.removeIf(c -> StringUtilities.isNullOrEmpty(c.getSources().stream().filter(s -> DataSourceComponent.QUERY_PARAMETER.equals(s.getName())).findFirst().get().getDataDescriptor().getLocation()));
        }
        return components;
    }

    @Override
    public List<DataSourceComponent> getBySourceAndSensor(String dataSourceName, String sensor) {
        return this.repository.getBySourceAndSensor(dataSourceName, sensor);
    }

    @Override
    public List<DataSourceComponent> getBySource(String dataSourceName) {
        return this.repository.getBySource(dataSourceName);
    }

    @Override
    public DataSourceComponent getQueryDataSourceComponent(long queryId) {
        return this.repository.getQueryDataSourceComponent(queryId);
    }

    @Override
    public void updateModified(DataSourceComponent component) throws PersistenceException {
        this.repository.updateModified(component, component.getId());
    }

    @Override
    public List<DataSourceComponent> getPRepositoryQueries(String userId) {
        return this.repository.getRepositoryQueries(userId);
    }

    @Override
    public List<String> getQueryPathsForRepository(String repositoryId) {
        return this.repository.getQueryPathsForRepository(repositoryId);
    }

    @Override
    public DataSourceComponent getRepositoryQuery(String repositoryId, String path) {
        return this.repository.getRepositoryQuery(repositoryId, path);
    }

    @Override
    public DataSourceComponent save(DataSourceComponent entity) throws PersistenceException {
        if (!checkEntity(entity, false)) {
            throw new PersistenceException(String.format("Invalid parameters provided for adding new entity of type %s!",
                                                         entity.getClass().getSimpleName()));
        }
        if (entity.getId() != null) {
            final Optional<DataSourceComponent> existing = repository.findById(entity.getId());
            if (existing.isPresent()) {
                throw new PersistenceException("There is already another entity with the identifier: " + entity.getId());
            }
        }
        List<SourceDescriptor> sources = entity.getSources();
        for (SourceDescriptor source : sources) {
            sourceDescriptorRepository.save(source);
        }
        List<TargetDescriptor> targets = entity.getTargets();
        for (TargetDescriptor target : targets) {
            targetDescriptorRepository.save(target);
        }
        return repository.save(entity);
    }

    @Override
    public DataSourceComponent update(DataSourceComponent entity) throws PersistenceException {
        if (!checkEntity(entity, true)) {
            throw new PersistenceException(String.format("Invalid parameters provided for updating the entity with identifier %s",
                                                         entity != null && entity.getId() != null ? entity.getId() : "<null>"));
        }

        final Optional<DataSourceComponent> existing = repository.findById(entity.getId());
        if (existing.isEmpty()) {
            throw new PersistenceException(String.format("There is no entity with the given identifier %s",
                                                         entity.getId()));
        }
        sourceDescriptorRepository.saveAll(entity.getSources());
        targetDescriptorRepository.saveAll(entity.getTargets());
        return repository.save(entity);
    }

    @Override
    public void delete(String id) throws PersistenceException {
        if (id == null || (id instanceof String && StringUtils.isEmpty(id.toString()))) {
            throw new PersistenceException("Invalid parameter provided for deleting the entity (empty identifier)");
        }

        final Optional<DataSourceComponent> existing = repository.findById(id);
        if (!existing.isEmpty()) {
            DataSourceComponent entity = existing.get();
            sourceDescriptorRepository.deleteAllInBatch(entity.getSources());
            targetDescriptorRepository.deleteAllInBatch(entity.getTargets());
            entity.setSources(null);
            entity.setTargets(null);
            entity = repository.saveAndFlush(entity);
            repository.delete(entity);
        }
    }

    @Override
    public void delete(Iterable<String> ids) throws PersistenceException {
        if (ids == null) {
            throw new PersistenceException("Invalid parameter provided for deleting the entities");
        }
        for (String id : ids) {
            delete(id);
        }
    }

    @Override
    protected boolean checkEntity(DataSourceComponent entity) {
        return checkComponent(entity) && entity.getSensorName() != null &&
                entity.getDataSourceName() != null && entity.getFetchMode() != null;
    }
}
