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

package ro.cs.tao.datasource.opensearch.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OpenSearchService {
    private String shortName;
    private String description;
    private String tags;
    private List<OpenSearchEndpoint> endpoints;

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public List<OpenSearchEndpoint> getEndpoints() { return endpoints; }
    public void addEndpoint(OpenSearchEndpoint endpoint) {
        if (this.endpoints == null) {
            this.endpoints = new ArrayList<>();
        }
        if (!this.endpoints.contains(endpoint)) {
            this.endpoints.add(endpoint);
        }
    }

    public OpenSearchEndpoint getEndpoint(String type) {
        return this.endpoints != null ?
                this.endpoints.stream().filter(e -> e.getType().equals(type)).findFirst().orElse(null) : null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenSearchService that = (OpenSearchService) o;
        return Objects.equals(shortName, that.shortName);
    }

    @Override
    public int hashCode() { return Objects.hash(shortName); }
}
