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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.component.*;
import ro.cs.tao.component.enums.ProcessingComponentType;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.ProcessingComponentProvider;
import ro.cs.tao.persistence.repository.ParameterDescriptorRepository;
import ro.cs.tao.persistence.repository.ParameterExpansionRuleRepository;
import ro.cs.tao.persistence.repository.ProcessingComponentRepository;
import ro.cs.tao.utils.StringUtilities;

import javax.sql.DataSource;
import java.util.*;

@Configuration
@EnableTransactionManagement
@Component("processingComponentManager")
public class ProcessingComponentManager extends TaoComponentManager<ProcessingComponent, ProcessingComponentRepository>
                                        implements ProcessingComponentProvider {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ParameterExpansionRuleRepository expansionRuleRepository;

    @Autowired
    private ParameterDescriptorRepository parameterDescriptorRepository;

    @Override
    public ProcessingComponent get(String id, String containerId) {
        return repository.getByIdAndContainer(id, containerId);
    }

    @Override
    public ProcessingComponent getByLabel(String label, String containerId) {
        return repository.getByLabelAndContainer(label, containerId);
    }

    /**
     * Retrieve active processing components with SYSTEM and CONTRIBUTOR visibility
     */
    @Override
    public List<ProcessingComponent> listUserProcessingComponents(String userId) {
        return repository.getUserComponentsByType(userId, ProcessingComponentType.EXECUTABLE.value());
    }

    @Override
    public List<ProcessingComponent> listUserScriptComponents(String userId) {
        return repository.getUserComponentsByType(userId, ProcessingComponentType.SCRIPT.value());
    }

    @Override
    public List<ProcessingComponent> listOtherComponents(Set<String> ids) {
        return repository.getOtherComponents(ids);
    }

    @Override
    public List<ProcessingComponent> listByContainer(String containerId) {
        return repository.getByContainer(containerId);
    }

    @Override
    public void updateModified(ProcessingComponent component) throws PersistenceException {
        this.repository.updateModified(component, component.getId());
    }

    @Override
    public boolean hasCopyComponent(String containerId) {
        return getByLabel("Copy", containerId) != null;
    }

    @Override
    public boolean hasMoveComponent(String containerId) {
        return getByLabel("Move", containerId) != null;
    }

    @Override
    public boolean hasDeleteComponent(String containerId) {
        return getByLabel("Delete", containerId) != null;
    }

    @Override
    public List<ProcessingComponent> listByLabel(String label) {
        return repository.getByLabel(label);
    }

    @Override
    public ProcessingComponent save(ProcessingComponent entity) throws PersistenceException {
        Set<ParameterDescriptor> descriptors = entity.getParameterDescriptors();
        Map<ParameterDescriptor, ParameterExpansionRule> map = new HashMap<>();
        if (descriptors != null) {
            descriptors.forEach(d -> {
                if (StringUtilities.isNullOrEmpty(d.getId())) {
                    d.setId(UUID.randomUUID().toString());
                }
                ParameterExpansionRule expansionRule = d.getExpansionRule();
                if (expansionRule != null && !d.getId().equals(expansionRule.getId())) {
                    expansionRule.setId(d.getId());
                    map.put(d, expansionRule);
                    d.setExpansionRule(null);
                }
            });
            for(ParameterDescriptor descriptor : descriptors){
                parameterDescriptorRepository.save(descriptor);
            }
        }
        if (!checkEntity(entity, false)) {
            throw new PersistenceException(String.format("Invalid parameters provided for adding new entity of type %s!",
                                                         entity.getClass().getSimpleName()));
        }
        if (entity.getId() != null && entity.getContainerId() != null) {
            final ProcessingComponent existing = repository.getByIdAndContainer(entity.getId(), entity.getContainerId());
            if (existing != null) {
                throw new PersistenceException("There is already another entity with the identifier: " + entity.getId());
            }
        }
        if (entity.getNodeAffinity() == null) {
            entity.setNodeAffinity(NodeAffinity.Any);
        }
        final List<SourceDescriptor> sources = entity.getSources();
        for (SourceDescriptor descriptor : sources) {
            final String formatName = descriptor.getDataDescriptor().getFormatName();
            if (!StringUtilities.isNullOrEmpty(formatName) && descriptor.getConstraints().contains("Specific sensor")) {
                descriptor.addConstraint("Specific sensor", formatName);
            }
        }
        repository.save(entity);
        if (!map.isEmpty()) {
            expansionRuleRepository.saveAll(map.values());
            descriptors.forEach(d -> {
                if (map.containsKey(d)) {
                    d.setExpansionRule(map.get(d));
                }
            });
            map.clear();
        }
        return entity;
    }

    @Override
    public ProcessingComponent update(ProcessingComponent entity) throws PersistenceException {
        if (!checkEntity(entity, true)) {
            throw new PersistenceException(String.format("Invalid parameters provided for updating entity of type %s!",
                                                         entity.getClass().getSimpleName()));
        }
        final Optional<ProcessingComponent> existing = repository.findById(entity.getId());
        if (existing.isEmpty()) {
            throw new PersistenceException(String.format("There is no entity with the given identifier %s",
                    entity.getId()));
        }
        ProcessingComponent entityExisting = existing.get();
        Set<ParameterDescriptor> descriptors = entity.getParameterDescriptors();
        Map<ParameterDescriptor, ParameterExpansionRule> map = new HashMap<>();
        if (descriptors != null) {
            descriptors.forEach(d -> {
                if (StringUtilities.isNullOrEmpty(d.getId())) {
                    d.setId(UUID.randomUUID().toString());
                }
                ParameterExpansionRule expansionRule = d.getExpansionRule();
                if (expansionRule != null && !d.getId().equals(expansionRule.getId())) {
                    expansionRule.setId(d.getId());
                    map.put(d, expansionRule);
                    d.setExpansionRule(null);
                }
            });
        }
        repository.save(entity);
        if (!map.isEmpty()) {
            expansionRuleRepository.saveAll(map.values());
            descriptors.forEach(d -> {
                if (map.containsKey(d)) {
                    d.setExpansionRule(map.get(d));
                }
            });
            map.clear();
        }
        return entity;
    }

    @Override
    protected boolean checkEntity(ProcessingComponent entity) {
        return checkComponent(entity) && entity.getFileLocation() != null &&
                entity.getTemplateType() != null && entity.getVisibility() != null &&
                entity.getParameterDescriptors().stream().allMatch(this::checkParameterDescriptor);
    }

    /**
     * Method to be used instead of the trigger that updates the "modified" column value in "tao.processing_component" table when a component is updated
     * @param processingComponentId - processing component ID
     * @return the number of rows affected
     */
    @Override
    public int updateModifiedDate(String processingComponentId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = " UPDATE component.processing_component SET modified = now() WHERE component.processing_component.id = ?";
        return jdbcTemplate.update(sql, processingComponentId);
    }

}
