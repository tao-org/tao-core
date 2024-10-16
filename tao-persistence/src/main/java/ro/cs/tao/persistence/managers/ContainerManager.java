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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.ContainerType;
import ro.cs.tao.docker.ContainerVisibility;
import ro.cs.tao.persistence.ContainerProvider;
import ro.cs.tao.persistence.repository.ContainerRepository;

import java.util.List;

@Configuration
@EnableTransactionManagement
@Component("containerManager")
public class ContainerManager extends EntityManager<Container, String, ContainerRepository> implements ContainerProvider {

    @Override
    public Container getByName(String name) {
        return repository.findByName(name);
    }

    @Override
    public List<Container> getByType(ContainerType type) {
        return repository.findByTypeId(type.value());
    }

    @Override
    public List<Container> getByTypeAndVisibility(ContainerType type, ContainerVisibility visibility) {
        return repository.findByTypeAndVisibility(type.value(), visibility.value());
    }

    @Override
    public List<Container> listContainersVisibleToUser(String userId) {
        return repository.listContainersVisibleToUser(userId);
    }

    @Override
    public List<Container> listUserContainers(String userId) {
        return repository.listUserContainers(userId);
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkEntity(Container entity, boolean existingEntity) {
        return entity != null && checkEntity(entity);
    }

    @Override
    protected boolean checkId(String entityId, boolean existingEntity) {
        return entityId != null && !entityId.isEmpty();
    }

    @Override
    protected boolean checkEntity(Container entity) {
        return entity.getName() != null && entity.getTag() != null &&
               entity.getApplications() != null && entity.getApplications().stream().allMatch(this::checkApplication);
    }

    private boolean checkApplication(Application application) {
        return application != null && application.getName() != null && !application.getName().isEmpty();
    }
}
