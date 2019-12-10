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

import ro.cs.tao.Sort;
import ro.cs.tao.SortDirection;
import ro.cs.tao.component.validation.ValidationException;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.utils.GenericComparator;

import java.util.*;

/**
 * Interface defining a CRUD entity service
 *
 * @author Cosmin Cara
 */
public interface CRUDService<T, K> extends TAOService {

    /**
     * Finds an entity by its identifier.
     *
     * @param id    The entity identifier
     * @return      An instance of the entity or <code>null</code> if not found
     */
    T findById(K id);

    /**
     * Retrieves all the entities of this type.
     */
    List<T> list();

    /**
     * Retrieves the entities of this type with the ids in the given collection.
     * @param ids   The collection of entity identifiers
     */
    List<T> list(Iterable<K> ids);

    /**
     * Retrieves a subset of the entities of this type, according to the <code>Sort</code> order provided.
     * The default implementation of this method uses the @see CRUDService.list() implementation.
     *
     * @param pageNumber The page number (starting with 1) of the paged query
     * @param pageSize  The size of a page
     * @param sort      The sort collection of fields and orders.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default List<T> list(Optional<Integer> pageNumber, Optional<Integer> pageSize, Sort sort) {
        int pNumber = pageNumber.orElse(0);
        int pSize = pageSize.orElse(0);
        if (pNumber < 0 || pSize < 0) {
            return null;
        }
        List<T> results = list();
        if (results == null || results.size() == 0 || pNumber * pSize > results.size()) {
            return new ArrayList<>();
        }
        int idx = 0;
        T obj = results.get(idx++);
        while (obj == null && idx < results.size()) {
            obj = results.get(idx++);
        }
        if (obj == null) {
            return new ArrayList<>();
        }
        Map<String, Boolean> sorts;
        if (sort != null && sort.getFieldsForSort() != null) {
            sorts = new LinkedHashMap<>();
            for (Map.Entry<String, SortDirection> entry : sort.getFieldsForSort().entrySet()) {
                sorts.put(entry.getKey(), SortDirection.ASC.equals(entry.getValue()));
            }
        } else {
            sorts = new HashMap<>();
        }
        results.sort(new GenericComparator(obj.getClass(), sorts));
        return pNumber > 0 && pSize > 0 ? results.subList((pNumber - 1) * pSize, pNumber * pSize) : results;
    }

    /**
     * Persists a new entity.
     *
     * @param object    The entity to be persisted.
     */
    T save(T object);

    /**
     * Updates an existing entity.
     *
     * @param object    The modified entity.
     */
    T update(T object) throws PersistenceException;

    /**
     * Removes the entity with the given identifier from the underlying persistent storage.
     *
     * @param id    The entity identifier.
     */
    void delete(K id) throws PersistenceException;

    /**
     * Validates the entity field values before persisting it.
     * Implementors should override this method which, by default, does nothing.
     *
     * @param object    The entity to validate.
     * @throws ValidationException  If inconsistent values are found.
     */
    default void validate(T object) throws ValidationException { }

    default T tag(K id, List<String> tags) throws PersistenceException { return null; }
    default T untag(K id, List<String> tags) throws PersistenceException { return null; }
}
