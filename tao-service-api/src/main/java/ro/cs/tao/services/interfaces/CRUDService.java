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
import ro.cs.tao.Tag;
import ro.cs.tao.component.validation.ValidationException;
import ro.cs.tao.persistence.PersistenceException;
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
     * Retrieves a subset of the entities of this type, optionally filtered by a label and tags, and ordered
     * according to the provided {@code Sort} parameter.
     *
     * @param pageNumber An {@code Optional} specifying the page number (starting with 1) of the paged query.
     * @param pageSize   An {@code Optional} specifying the size of the page.
     * @param sort       The sorting criteria, represented by a {@code Sort} object.
     * @param label      A string representing the label used to filter the entities.
     * @param tags       A list of {@code Tag} objects used to filter the entities.
     * @return           A {@code List} of entities that match the provided criteria.
     */
    default List<T> list(Optional<Integer> pageNumber, Optional<Integer> pageSize, Sort sort,
                         String label, List<Tag> tags) {
        return list(pageNumber, pageSize, sort);
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

    /**
     * Associates a list of tags with the entity identified by the given identifier.
     *
     * @param id   The identifier of the entity to be tagged.
     * @param tags A list of tags to be associated with the entity.
     * @return The updated entity instance with the provided tags or {@code null} if the operation fails.
     * @throws PersistenceException If an error occurs during the operation.
     */
    default T tag(K id, List<String> tags) throws PersistenceException { return null; }

    /**
     * Removes the specified list of tags from the entity identified by the given identifier.
     *
     * @param id   The identifier of the entity from which the tags will be removed.
     * @param tags A list of tags to be removed from the entity.
     * @return The updated entity instance after the tags have been removed, or {@code null} if the operation fails.
     * @throws PersistenceException If an error occurs during the operation.
     */
    default T unTag(K id, List<String> tags) throws PersistenceException { return null; }

    /**
     * Returns the total count of entities managed by the service.
     *
     * @return the total number of entities available.
     */
    default long count() { return 0L; }

    /**
     * Returns the count of entities that match the given label and tags.
     *
     * @param label A string representing the label of the entities to be counted.
     * @param tags  A list of {@code Tag} objects used to filter the entities to be counted.
     * @return      The total number of matching entities.
     */
    default long count(String label, List<Tag> tags) { return count(); }

    /**
     * Finds entities based on their similarity to the given text.
     *
     * @param text   The reference text to be used for similarity matching.
     * @param userId The identifier of the user requesting the operation.
     * @return A list of entities that are similar to the provided text.
     */
    default List<?> findBySimilarity(String text, String userId) { return List.of(); }

    /**
     * Finds labels by their similarity to the provided text for the specified user.
     *
     * @param text   The reference text to be used for similarity matching.
     * @param userId The identifier of the user requesting the operation.
     * @return A list of labels similar to the provided text.
     */
    default List<String> findLabelsBySimilarity(String text, String userId) { return List.of(); }
}
