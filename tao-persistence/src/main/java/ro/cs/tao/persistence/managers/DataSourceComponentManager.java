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
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.persistence.DataSourceComponentProvider;
import ro.cs.tao.persistence.repository.DataSourceComponentRepository;
import ro.cs.tao.utils.StringUtilities;

import java.util.List;
import java.util.Set;

@Component("dataSourceComponentManager")
public class DataSourceComponentManager extends TaoComponentManager<DataSourceComponent, DataSourceComponentRepository>
                                        implements DataSourceComponentProvider {

    @Override
    public List<DataSourceComponent> getUserDataSourceComponents(String userId) {
        return userId != null ? this.repository.getUserDataSourceComponents(userId) :
               this.repository.getUserDataSourceComponents();
    }

    @Override
    public List<DataSourceComponent> getSystemDataSourceComponents() {
        return this.repository.getSystemDataSourceComponents();
    }

    @Override
    public List<DataSourceComponent> getOtherDataSourceComponents(Set<String> ids) {
        return this.repository.getOtherDataSourceComponents(ids);
    }

    @Override
    public DataSourceComponent getDataSourceComponentByLabel(String label) {
        List<DataSourceComponent> components = this.repository.getDataSourceComponentByLabel(label);
        return (components == null || components.isEmpty()) ? null : components.get(0);
    }

    @Override
    public List<DataSourceComponent> getProductSets(String userId) {
        List<DataSourceComponent> components = this.repository.getUserDataSourceComponents(userId);
        if (components != null) {
            components.removeIf(c -> StringUtilities.isNullOrEmpty(c.getSources().stream().filter(s -> DataSourceComponent.QUERY_PARAMETER.equals(s.getName())).findFirst().get().getDataDescriptor().getLocation()));
        }
        return components;
    }

    @Override
    public List<DataSourceComponent> getBySourceAndSensor(String dataSourceName, String sensor) {
        return this.repository.getBySourceAndSensor(dataSourceName, sensor);
    }

    @Override
    public List<DataSourceComponent> getBySource(String dataSourceName) {
        return this.repository.getBySource(dataSourceName);
    }

    @Override
    public DataSourceComponent getQueryDataSourceComponent(long queryId) {
        return this.repository.getQueryDataSourceComponent(queryId);
    }

    @Override
    protected boolean checkId(String entityId, boolean existingEntity) {
        return entityId != null && !entityId.isEmpty();
    }

    @Override
    protected boolean checkEntity(DataSourceComponent entity) {
        return checkComponent(entity) && entity.getSensorName() != null &&
                entity.getDataSourceName() != null && entity.getFetchMode() != null;
    }
}
