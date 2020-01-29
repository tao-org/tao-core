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
import ro.cs.tao.component.GroupComponent;
import ro.cs.tao.component.enums.ProcessingComponentVisibility;
import ro.cs.tao.persistence.GroupComponentProvider;
import ro.cs.tao.persistence.repository.GroupComponentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component("groupComponentManager")
public class GroupComponentManager extends TaoComponentManager<GroupComponent, GroupComponentRepository>
                                    implements GroupComponentProvider {

    /**
     * Retrieve active group components with SYSTEM and CONTRIBUTOR visibility
     */
    @Override
    public List<GroupComponent> list() {
        return super.list().stream()
                .filter(c -> (ProcessingComponentVisibility.SYSTEM.value().equals(c.getVisibility().value()) ||
                        ProcessingComponentVisibility.CONTRIBUTOR.value().equals(c.getVisibility().value())) &&
                        c.getActive())
                .collect(Collectors.toList());
    }

    @Override
    protected boolean checkEntity(GroupComponent entity) {
        return checkComponent(entity) && entity.getVisibility() != null;
    }
}
