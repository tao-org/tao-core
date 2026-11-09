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

package ro.cs.tao.datasource.opensearch.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents an OpenSearch service that provides metadata and endpoints
 * required for interacting with the OpenSearch Description Document (OSDD).
 * This class encapsulates the service's short name, description, tags,
 * and associated endpoints with their respective types.
 *
 * The {@code OpenSearchService} primarily acts as a container for the metadata and endpoints
 * and provides utility methods for managing and retrieving endpoints by type.
 *
 * Features of the OpenSearchService:
 * - Stores metadata such as short name, description, and tags.
 * - Maintains a list of {@code OpenSearchEndpoint} instances for different endpoint types.
 * - Provides methods to add endpoints and retrieve filtered lists of endpoints by type.
 * - Supports equality and hash code operations based on the {@code shortName}.
 *
 * Note:
 * - The {@code shortName} is expected to uniquely identify the service.
 * - The endpoints list can dynamically grow as new endpoints are added.
 */
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

    public List<OpenSearchEndpoint> getEndpoints(String type) {
        return this.endpoints != null ?
                this.endpoints.stream().filter(e -> e.getType().equals(type)).collect(Collectors.toList()) : null;
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
