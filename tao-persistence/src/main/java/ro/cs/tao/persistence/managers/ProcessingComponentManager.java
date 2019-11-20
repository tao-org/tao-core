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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ParameterExpansionRule;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.enums.ProcessingComponentType;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.ParameterExpansionRuleRepository;
import ro.cs.tao.persistence.repository.ProcessingComponentRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("processingComponentManager")
public class ProcessingComponentManager extends TaoComponentManager<ProcessingComponent, ProcessingComponentRepository> {

    @Autowired
    private ParameterExpansionRuleRepository expansionRuleRepository;

    /**
     * Retrieve active processing components with SYSTEM and CONTRIBUTOR visibility
     */
    @Transactional
    public List<ProcessingComponent> getUserProcessingComponents(String userName) {
        return repository.getUserComponentsByType(userName, ProcessingComponentType.EXECUTABLE.value());
    }

    @Transactional
    public List<ProcessingComponent> getUserScriptComponents(String userName) {
        return repository.getUserComponentsByType(userName, ProcessingComponentType.SCRIPT.value());
    }

    public List<ProcessingComponent> getProcessingComponentsByLabel(String label) {
        return repository.getByLabel(label);
    }

    @Override
    public ProcessingComponent save(ProcessingComponent entity) throws PersistenceException {
        List<ParameterDescriptor> descriptors = entity.getParameterDescriptors();
        Map<ParameterDescriptor, ParameterExpansionRule> map = new HashMap<>();
        if (descriptors != null) {
            descriptors.forEach(d -> {
                ParameterExpansionRule expansionRule = d.getExpansionRule();
                if (expansionRule != null && !d.getId().equals(expansionRule.getId())) {
                    expansionRule.setId(d.getId());
                    map.put(d, expansionRule);
                    d.setExpansionRule(null);
                }
            });
        }
        super.save(entity);
        if (map.size() > 0) {
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
        return super.update(entity);
    }

    @Override
    protected boolean checkEntity(ProcessingComponent entity) {
        return checkComponent(entity) && entity.getFileLocation() != null &&
                entity.getTemplateType() != null && entity.getVisibility() != null &&
                entity.getParameterDescriptors().stream().allMatch(this::checkParameterDescriptor);
    }

}
