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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ParameterExpansionRule;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.enums.ProcessingComponentType;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.ProcessingComponentProvider;
import ro.cs.tao.persistence.repository.ParameterExpansionRuleRepository;
import ro.cs.tao.persistence.repository.ProcessingComponentRepository;
import ro.cs.tao.utils.StringUtilities;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@EnableTransactionManagement
@Component("processingComponentManager")
public class ProcessingComponentManager extends TaoComponentManager<ProcessingComponent, ProcessingComponentRepository>
                                        implements ProcessingComponentProvider {

    @Autowired
    private ParameterExpansionRuleRepository expansionRuleRepository;

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
        }
        //super.save(entity);
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
        final Set<ParameterDescriptor> descriptors = entity.getParameterDescriptors();
        if (descriptors != null) {
            for (ParameterDescriptor descriptor : descriptors) {
                if (StringUtilities.isNullOrEmpty(descriptor.getId())) {
                    descriptor.setId(UUID.randomUUID().toString());
                }
            }
        }
        Map<ParameterDescriptor, ParameterExpansionRule> rules = descriptors.stream()
                                                                            .filter(p -> p.getExpansionRule() != null)
                                                                            .collect(Collectors.toMap(Function.identity(),
                                                                                                 ParameterDescriptor::getExpansionRule));
        for (Map.Entry<ParameterDescriptor, ParameterExpansionRule> entry : rules.entrySet()) {
            ParameterExpansionRule rule = entry.getValue();
            rule.setId(entry.getKey().getId());
            expansionRuleRepository.save(rule);
        }
        repository.save(entity);
        return entity;
    }

    @Override
    protected boolean checkEntity(ProcessingComponent entity) {
        return checkComponent(entity) && entity.getFileLocation() != null &&
                entity.getTemplateType() != null && entity.getVisibility() != null &&
                entity.getParameterDescriptors().stream().allMatch(this::checkParameterDescriptor);
    }

}
