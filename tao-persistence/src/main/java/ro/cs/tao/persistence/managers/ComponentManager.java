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

package ro.cs.tao.persistence.managers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.GroupComponent;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "ro.cs.tao.persistence.repository" })
@Component("componentManager")
public class ComponentManager {

    private Logger logger = Logger.getLogger(ComponentManager.class.getName());

    /** CRUD Repository for Container entities */
    @Autowired
    private ContainerRepository containerRepository;

    /** CRUD Repository for ProcessingComponent entities */
    @Autowired
    private ProcessingComponentRepository processingComponentRepository;

    /** CRUD Repository for GroupComponent entities */
    @Autowired
    private GroupComponentRepository groupComponentRepository;

    /** CRUD Repository for DataSourceComponent entities */
    @Autowired
    private DataSourceComponentRepository dataSourceComponentRepository;

    /** CRUD Repository for ParameterDescriptor entities */
    @Autowired
    private ParameterDescriptorRepository parameterDescriptorRepository;

    //region Container and Applications
    /**
     * Retrieve existing containers
     */
    @Transactional(readOnly = true)
    public List<Container> getContainers() {
        // retrieve containers and filter them
        return new ArrayList<>((List<Container>) containerRepository.findAll(new Sort(Sort.Direction.ASC,
                                                                                      Constants.CONTAINER_IDENTIFIER_PROPERTY_NAME)));
    }

    /**
     * Retrieve container by its identifier
     */
    //@Transactional(readOnly = true)
    public Container getContainerById(String id) throws PersistenceException {
        // check method parameters
        if (id == null || StringUtils.isEmpty(id)) {
            throw new PersistenceException("Invalid parameter was provided for retrieving a container (empty identifier)");
        }

        // check if there is such container (to retrieve) with the given identifier
        final Container existingContainer = containerRepository.findById(id);
        if (existingContainer == null) {
            throw new PersistenceException("There is no container with the given identifier: " + id);
        }

        return existingContainer;
    }

    @Transactional
    public Container saveContainer(Container container) throws PersistenceException {
        // check method parameters
        if (!checkContainer(container)) {
            throw new PersistenceException("Invalid parameters were provided for adding new container !");
        }

        // check if there is already another container with the same identifier
        final Container containerWithSameId = containerRepository.findById(container.getId());
        if (containerWithSameId != null) {
            throw new PersistenceException("There is already another container with the identifier: " + container.getId());
        }

        // save the new Container entity and return it
        return containerRepository.save(container);
    }

    @Transactional
    public Container updateContainer(Container container) throws PersistenceException {
        // check method parameters
        if (!checkContainer(container)) {
            throw new PersistenceException("Invalid parameters were provided for updating the container "
                                                   + (container != null && container.getId() != null ? "(identifier " + container.getId() + ")" : "") + "!");
        }

        // check if there is such container (to update) with the given identifier
        final Container existingContainer = containerRepository.findById(container.getId());
        if (existingContainer == null) {
            throw new PersistenceException("There is no container with the given identifier: " + container.getId());
        }

        return containerRepository.save(container);
    }

    /**
     * Retrieve container by its identifier
     */
    @Transactional(readOnly = true)
    public boolean checkIfExistsContainerById(String id) throws PersistenceException {
        boolean result = false;
        // check method parameters
        if(id == null || StringUtils.isEmpty(id)) {
            throw new PersistenceException("Invalid parameter was provided for verifying a container existence (empty identifier)");
        }

        // check if there is such container (to retrieve) with the given identifier
        final Container existingContainer = containerRepository.findById(id);
        if (existingContainer != null) {
            result = true;
        }

        return result;
    }

    /**
     * Delete container
     * @param id - container identifier
     */
    @Transactional
    public void deleteContainer(String id) throws PersistenceException {
        // check method parameters
        if (id == null || StringUtils.isEmpty(id)) {
            throw new PersistenceException("Invalid parameter was provided for deleting a container (empty identifier)");
        }

        // check if there is such container (to delete) with the given identifier
        final Container existingContainer = containerRepository.findById(id);
        if (existingContainer == null) {
            throw new PersistenceException("There is no container with the given identifier: " + id);
        }

        containerRepository.delete(existingContainer);
    }
    //endregion

    //region ProcessingComponent
    /**
     * Retrieve active processing components with SYSTEM and CONTRIBUTOR visibility
     */
    @Transactional(readOnly = true)
    public List<ProcessingComponent> getProcessingComponents()
    {
        // retrieve components and filter them
        return ((List<ProcessingComponent>)
                processingComponentRepository.findAll(new Sort(Sort.Direction.ASC,
                                                               Constants.COMPONENT_IDENTIFIER_PROPERTY_NAME)))
                .stream()
                .filter(c -> (c.getVisibility().getValue() == ProcessingComponentVisibility.SYSTEM.getValue() ||
                        c.getVisibility().getValue() == ProcessingComponentVisibility.CONTRIBUTOR.getValue()) &&
                        c.getActive())
                .collect(Collectors.toList());
    }

    @Transactional
    public ProcessingComponent getProcessingComponentById(final String id) throws PersistenceException {
        // check method parameters
        if (id == null || id.isEmpty()) {
            throw new PersistenceException("Invalid parameters were provided for searching processing component by identifier ("+ String.valueOf(id) +") !");
        }

        // retrieve ProcessingComponent after its identifier
        final ProcessingComponent componentEnt = processingComponentRepository.findById(id);
        if (componentEnt == null) {
            throw new PersistenceException("There is no processing component with the specified identifier: " + id);
        }

        return componentEnt;
    }

    @Transactional
    public ProcessingComponent saveProcessingComponent(ProcessingComponent component) throws PersistenceException {
        // check method parameters
        if (!checkProcessingComponent(component)) {
            throw new PersistenceException("Invalid parameters were provided for adding new processing component !");
        }

        // check if there is already another component with the same identifier
        final ProcessingComponent componentWithSameId = processingComponentRepository.findById(component.getId());
        if (componentWithSameId != null) {
            throw new PersistenceException("There is already another component with the identifier: " + component.getId());
        }

        // save the new ProcessingComponent entity and return it
        return processingComponentRepository.save(component);
    }

    @Transactional
    public ProcessingComponent updateProcessingComponent(ProcessingComponent component) throws PersistenceException {
        // check method parameters
        if (!checkProcessingComponent(component)) {
            throw new PersistenceException("Invalid parameters were provided for updating the processing component "
                                                   + (component != null && component.getId() != null ? "(identifier " + component.getId() + ")" : "") + "!");
        }

        // check if there is such component (to update) with the given identifier
        final ProcessingComponent existingComponent = processingComponentRepository.findById(component.getId());
        if (existingComponent == null) {
            throw new PersistenceException("There is no processing component with the given identifier: " + component.getId());
        }

        return processingComponentRepository.save(component);
    }

    @Transactional(readOnly = true)
    public boolean checkIfExistsComponentById(final String id) {
        boolean result = false;
        if (id != null && !id.isEmpty()) {
            // try to retrieve ProcessingComponent after its identifier
            final ProcessingComponent componentEnt = processingComponentRepository.findById(id);
            if (componentEnt != null) {
                result = true;
            }
        }

        return result;
    }

    @Transactional
    public ProcessingComponent deleteProcessingComponent(final String id) throws PersistenceException {
        // check method parameters
        if (id == null || id.isEmpty()) {
            throw new PersistenceException("Invalid parameters were provided for deleting processing component (id\""+ String.valueOf(id) +"\") !");
        }

        // retrieve ProcessingComponent after its id
        final ProcessingComponent componentEnt = processingComponentRepository.findById(id);
        if (componentEnt == null) {
            throw new PersistenceException("There is no processing component with the specified id: " + id);
        }

        // deactivate the processing component
        componentEnt.setActive(false);

        // save it
        return processingComponentRepository.save(componentEnt);
    }

    /**
     * TODO (User entity from data model)
     * Retrieve processing components with USER visibility, for a given user
     * @return
     */
    /*@Transactional(readOnly = true)
    public List<ProcessingComponent> getUserProcessingComponents(User user)
    {
        final List<ProcessingComponent> components = new ArrayList<>();
        // retrieve components and filter them
        components.addAll(((List<ProcessingComponent>) processingComponentRepository.findAll(new Sort(Sort.Direction.ASC, COMPONENT_IDENTIFIER_PROPERTY_NAME))).stream()
          // TODO c.getUser() = user  =>  add user property on component
          .filter(c -> c.getVisibility().equals(ProcessingComponentVisibility.USER))
          .collect(Collectors.toList()));
        return components;
    }*/
    //endregion

    //region GroupComponent
    /**
     * Retrieve active group components with SYSTEM and CONTRIBUTOR visibility
     */
    @Transactional(readOnly = true)
    public List<GroupComponent> getGroupComponents() {
        // retrieve components and filter them
        return ((List<GroupComponent>)
                groupComponentRepository.findAll(new Sort(Sort.Direction.ASC,
                                                          Constants.COMPONENT_IDENTIFIER_PROPERTY_NAME)))
                .stream()
                .filter(c -> (c.getVisibility().getValue() == ProcessingComponentVisibility.SYSTEM.getValue() ||
                        c.getVisibility().getValue() == ProcessingComponentVisibility.CONTRIBUTOR.getValue()) &&
                        c.getActive())
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupComponent getGroupComponentById(final String id) throws PersistenceException {
        // check method parameters
        if (id == null || id.isEmpty()) {
            throw new PersistenceException("Invalid parameters were provided for searching group component by identifier ("+ String.valueOf(id) +") !");
        }

        // retrieve ProcessingComponent after its identifier
        final GroupComponent componentEnt = groupComponentRepository.findById(id);
        if (componentEnt == null) {
            throw new PersistenceException("There is no group component with the specified identifier: " + id);
        }

        return componentEnt;
    }

    @Transactional
    public GroupComponent deleteGroupComponent(final String id) throws PersistenceException {
        // check method parameters
        if (id == null || id.isEmpty()) {
            throw new PersistenceException("Invalid parameters were provided for deleting group component (id\""+ String.valueOf(id) +"\") !");
        }

        // retrieve GroupComponent after its id
        final GroupComponent componentEnt = groupComponentRepository.findById(id);
        if (componentEnt == null) {
            throw new PersistenceException("There is no group component with the specified id: " + id);
        }

        // deactivate the processing component
        componentEnt.setActive(false);

        // save it
        return groupComponentRepository.save(componentEnt);
    }

    @Transactional
    public GroupComponent saveGroupComponent(GroupComponent component) throws PersistenceException {
        // check method parameters
        if (!checkGroupComponent(component)) {
            throw new PersistenceException("Invalid parameters were provided for adding new group component !");
        }

        // check if there is already another component with the same identifier
        final GroupComponent componentWithSameId = groupComponentRepository.findById(component.getId());
        if (componentWithSameId != null) {
            throw new PersistenceException("There is already another group component with the identifier: " + component.getId());
        }

        // save the new GroupComponent entity and return it
        return groupComponentRepository.save(component);
    }

    @Transactional
    public GroupComponent updateGroupComponent(GroupComponent component) throws PersistenceException {
        // check method parameters
        if (!checkGroupComponent(component)) {
            throw new PersistenceException("Invalid parameters were provided for updating the group component "
                                                   + (component != null && component.getId() != null ? "(identifier " + component.getId() + ")" : "") + "!");
        }

        // check if there is such component (to update) with the given identifier
        final GroupComponent existingComponent = groupComponentRepository.findById(component.getId());
        if (existingComponent == null) {
            throw new PersistenceException("There is no group component with the given identifier: " + component.getId());
        }

        return groupComponentRepository.save(component);
    }
    //endregion

    //region DataSourceComponent
    /**
     * Retrieve data sources components
     */
    @Transactional(readOnly = true)
    public List<DataSourceComponent> getDataSourceComponents() {
        // retrieve components and filter them
        return new ArrayList<>(
                (List<DataSourceComponent>)
                        dataSourceComponentRepository.findAll(new Sort(Sort.Direction.ASC,
                                                                       Constants.COMPONENT_IDENTIFIER_PROPERTY_NAME)));
    }

    @Transactional(readOnly = true)
    public DataSourceComponent getDataSourceInstance(String id) {
        return dataSourceComponentRepository.findOne(id);
    }

    @Transactional
    public DataSourceComponent saveDataSourceComponent(DataSourceComponent component) throws PersistenceException {
        // check method parameters
        if(!checkDataSourceComponent(component)) {
            throw new PersistenceException("Invalid parameters were provided for adding new data source component !");
        }

        // check if there is already another component with the same identifier
        final DataSourceComponent componentWithSameId = dataSourceComponentRepository.findById(component.getId());
        if (componentWithSameId != null) {
            throw new PersistenceException("There is already another component with the identifier: " + component.getId());
        }

        // save the new DataSourceComponent entity and return it
        return dataSourceComponentRepository.save(component);
    }
    //endregion

    private boolean checkContainer(Container container) {
        return container != null && container.getId() != null && !container.getId().isEmpty() &&
                container.getName() != null && container.getTag() != null &&
                container.getApplications() != null && container.getApplications().stream().allMatch(this::checkApplication);
    }

    private boolean checkApplication(Application application) {
        return application != null && application.getName() != null && !application.getName().isEmpty();
    }

    private boolean checkComponent(TaoComponent component) {
        return component != null && (component.getId() != null && !component.getId().isEmpty()) &&
                component.getLabel() != null && component.getVersion() != null &&
                component.getDescription() != null && component.getAuthors() != null &&
                component.getCopyright() != null;
    }

    private boolean checkProcessingComponent(ProcessingComponent component) {
        return checkComponent(component) && component.getFileLocation() != null &&
                component.getTemplateType() != null && component.getVisibility() != null &&
                component.getParameterDescriptors().stream().allMatch(this::checkParameterDescriptor);
    }

    private boolean checkGroupComponent(GroupComponent component) {
        return checkComponent(component) && component.getVisibility() != null;
    }

    private boolean checkDataSourceComponent(DataSourceComponent component) {
        return checkComponent(component) && component.getSensorName() != null &&
                component.getDataSourceName() != null && component.getFetchMode() != null;
    }

    private boolean checkParameterDescriptor(ParameterDescriptor parameterDesc) {
        return parameterDesc != null && (parameterDesc.getId() != null && !parameterDesc.getId().isEmpty()) &&
                parameterDesc.getType() != null && parameterDesc.getDataType() != null &&
                parameterDesc.getLabel() != null && !parameterDesc.getLabel().isEmpty();
    }
}
