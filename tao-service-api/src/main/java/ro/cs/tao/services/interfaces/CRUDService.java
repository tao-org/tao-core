/*
 * Copyright (C) 2017 CS ROMANIA
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

import ro.cs.tao.component.validation.ValidationException;
import ro.cs.tao.persistence.exception.PersistenceException;

import java.util.List;

/**
 * Interface defining a CRUD entity service
 *
 * @author Cosmin Cara
 */
public interface CRUDService<T> {

    /**
     * Finds an entity by its identifier.
     *
     * @param id    The entity identifier
     * @return      An instance of the entity or <code>null</code> if not found
     */
    T findById(String id) throws PersistenceException;

    /**
     * Retrieves all the entities of this type.
     */
    List<T> list();

    /**
     * Persists a new entity.
     *
     * @param object    The entity to be persisted.
     */
    void save(T object);

    /**
     * Updates an existing entity.
     *
     * @param object    The modified entity.
     */
    void update(T object);

    /**
     * Removes the entity with the given identifier from the underlying persistent storage.
     *
     * @param id    The entity identifier.
     */
    void delete(String id) throws PersistenceException;

    /**
     * Validates the entity field values before persisting it.
     * Implementors should override this method which, by default, does nothing.
     *
     * @param object    The entity to validate.
     * @throws ValidationException  If inconsistent values are found.
     */
    default void validate(T object) throws ValidationException { }
}
