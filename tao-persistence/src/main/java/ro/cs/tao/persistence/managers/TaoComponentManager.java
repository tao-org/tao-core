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

import org.springframework.data.repository.PagingAndSortingRepository;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.TaoComponent;

import java.util.UUID;

public abstract class TaoComponentManager<T extends TaoComponent, R extends PagingAndSortingRepository<T, String>>
        extends EntityManager<T, String, R> {

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkId(String entityId, boolean existingEntity) {
        return entityId != null && !entityId.isEmpty();
    }

    @Override
    protected boolean checkEntity(T entity, boolean existingEntity) {
        return entity != null && checkEntity(entity);
    }

    protected boolean checkComponent(TaoComponent component) {
        return component.getLabel() != null && component.getVersion() != null &&
                component.getDescription() != null && component.getAuthors() != null &&
                component.getCopyright() != null;
    }

    protected boolean checkParameterDescriptor(ParameterDescriptor parameterDesc) {
        if (parameterDesc != null) {
            if (parameterDesc.getId() == null) {
                parameterDesc.setId(UUID.randomUUID().toString());
            }
        }
        return parameterDesc != null && (parameterDesc.getId() != null && !parameterDesc.getId().isEmpty()) &&
                (parameterDesc.getName() != null && !parameterDesc.getName().isEmpty()) &&
                parameterDesc.getType() != null && parameterDesc.getDataType() != null &&
                parameterDesc.getLabel() != null && !parameterDesc.getLabel().isEmpty();
    }
}
