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

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.enums.ProcessingComponentType;
import ro.cs.tao.persistence.repository.ProcessingComponentRepository;

import java.util.List;

@Component("processingComponentManager")
public class ProcessingComponentManager extends TaoComponentManager<ProcessingComponent, ProcessingComponentRepository> {

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
    protected boolean checkEntity(ProcessingComponent entity) {
        return checkComponent(entity) && entity.getFileLocation() != null &&
                entity.getTemplateType() != null && entity.getVisibility() != null &&
                entity.getParameterDescriptors().stream().allMatch(this::checkParameterDescriptor);
    }

}
