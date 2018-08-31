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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.SortDirection;
import ro.cs.tao.component.Identifiable;
import ro.cs.tao.persistence.exception.PersistenceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
public abstract class EntityManager<T extends Identifiable<K>, K, R extends PagingAndSortingRepository<T, K>> {

    @Autowired
    protected R repository;

    @Transactional
    public List<T> list() {
        return new ArrayList<>((List<T>) repository.findAll(new Sort(Sort.Direction.ASC,
                                                                     identifier())));
    }

    @Transactional
    public List<T> list(Iterable<K> ids) {
        return new ArrayList<>((List<T>) repository.findAllById(ids));
    }

    @Transactional
    public List<T> list(ro.cs.tao.Sort sort) {
        Sort jpaSort;
        if (sort != null && sort.getFieldsForSort() != null && sort.getFieldsForSort().size() > 0) {
            List<Sort.Order> orders = new ArrayList<>();
            for (Map.Entry<String, SortDirection> entry : sort.getFieldsForSort().entrySet()) {
                orders.add(new Sort.Order(SortDirection.ASC.equals(entry.getValue()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                                          entry.getKey()));
            }
            jpaSort = Sort.by(orders);
        } else {
            jpaSort = new Sort(Sort.Direction.ASC, identifier());
        }
        return new ArrayList<>((List<T>) repository.findAll(jpaSort));
    }

    @Transactional
    public List<T> list(int pageNumber, int pageSize, ro.cs.tao.Sort sort) {
        List<T> results = null;
        if (pageNumber <= 0 || pageSize <= 0) {
            results = new ArrayList<>();
        }
        Sort jpaSort;
        if (sort != null && sort.getFieldsForSort() != null && sort.getFieldsForSort().size() > 0) {
            List<Sort.Order> orders = new ArrayList<>();
            for (Map.Entry<String, SortDirection> entry : sort.getFieldsForSort().entrySet()) {
                orders.add(new Sort.Order(SortDirection.ASC.equals(entry.getValue()) ? Sort.Direction.ASC : Sort.Direction.DESC,
                                          entry.getKey()));
            }
            jpaSort = Sort.by(orders);
        } else {
            jpaSort = new Sort(Sort.Direction.ASC, identifier());
        }
        Page<T> page = repository.findAll(PageRequest.of(pageNumber, pageSize, jpaSort));
        if (page.hasContent()) {
            results = page.getContent();
        } else {
            results = new ArrayList<>();
        }
        return results;
    }

    @Transactional
    public T get(K id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public boolean exists(K id) {
        return get(id) != null;
    }

    @Transactional
    public T save(T entity) throws PersistenceException {
        if (!checkEntity(entity, false)) {
            throw new PersistenceException("Invalid parameters provided for adding new entity!");
        }
        if (entity.getId() != null) {
            final Optional<T> existing = repository.findById(entity.getId());
            if (existing.isPresent()) {
                throw new PersistenceException("There is already another entity with the identifier: " + entity.getId());
            }
        }
        return repository.save(entity);
    }

    @Transactional
    public T update(T entity) throws PersistenceException {
        if (!checkEntity(entity, true)) {
            throw new PersistenceException(String.format("Invalid parameters provided for updating the entity with identifier %s",
                                                         entity != null && entity.getId() != null ? entity.getId() : "<null>"));
        }

        final Optional<T> existing = repository.findById(entity.getId());
        if (!existing.isPresent()) {
            throw new PersistenceException(String.format("There is no entity with the given identifier %s",
                                                         entity.getId()));
        }
        return repository.save(entity);
    }

    @Transactional
    public T delete(K id) throws PersistenceException {
        if (id == null || (id instanceof String && StringUtils.isEmpty(id.toString()))) {
            throw new PersistenceException("Invalid parameter provided for deleting the entity (empty identifier)");
        }

        final Optional<T> existing = repository.findById(id);
        if (!existing.isPresent()) {
            throw new PersistenceException(String.format("There is no entity with the given identifier %s", id));
        }
        T entity = existing.get();
        repository.delete(entity);
        return entity;
    }

    @Transactional
    public T delete(T entity) throws PersistenceException {
        if (entity == null || (entity.getId() instanceof String && StringUtils.isEmpty(entity.getId().toString()))) {
            throw new PersistenceException("Invalid parameter provided for deleting the entity (empty identifier)");
        }
        delete(entity.getId());
        return entity;
    }

    protected abstract String identifier();

    protected abstract boolean checkEntity(T entity);

    protected abstract boolean checkId(K entityId, boolean existingEntity);

    protected boolean checkEntity(T entity, boolean existingEntity) {
        return entity != null && checkId(entity.getId(), existingEntity) && checkEntity(entity);
    }

}
