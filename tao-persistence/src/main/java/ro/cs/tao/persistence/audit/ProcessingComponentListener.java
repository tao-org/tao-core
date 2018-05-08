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

package ro.cs.tao.persistence.audit;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.persistence.managers.SimpleCache;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

public class ProcessingComponentListener {

    @PostPersist
    @PostUpdate
    public void onChange(ProcessingComponent component) {
        SimpleCache.Cache<String, ProcessingComponent> cache = SimpleCache.getCache(ProcessingComponent.class);
        if (cache != null) {
            cache.put(component.getId(), component);
        }
    }

    @PostRemove
    public void onRemove(ProcessingComponent component) {
        SimpleCache.Cache<String, ProcessingComponent> cache = SimpleCache.getCache(ProcessingComponent.class);
        if (cache != null) {
            cache.remove(component.getId());
        }
    }
}
