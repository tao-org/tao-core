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

package ro.cs.tao.services.model.geostormcatalog;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.geojson.geom.GeometryJSON;

import java.io.IOException;

/**
 * Class resource mapped on Geostorm Resource (same fields names) used for REST operations purposes
 *
 * @author Oana H.
 */
public class Resource {

    /**
     * Execution identifier, type int, MANDATORY
     */
    private int execution_id = 0;
    /**
     * Path to the created product, MANDATORY
     */
    private String data_path;
    /**
     * Type of product, "input" or "output", MANDATORY
     */
    private String data_type;
    /**
     * Product name, type string, MANDATORY
     * Format: <collection_upper_case> <process_name>
     * <collection_upper_case> is input product collection identifier (backend "name" field)
     * <process_name> is current process name
     */
    private String name;
    /**
     * Product short description, MANDATORY
     * Format: name + " process output"
     */
    private String short_description;

    private String resource_storage_type;

    private boolean managed_resource_storage = false;

    private String organisation;

    private String usage;
    /**
     * Product release date, type string
     * Format YYYY-mm-dd
     */
    private String release_date;
    /**
     * bounding box (EPSG:4326 GeoJSON representation), type string (needed In Geostorm internally)
     */
    private Geometry wkb_geometry;

    private String collection;

    private String entry_point;

    private Integer[] coord_sys;

    public Resource() {
    }

    public int getExecution_id() {
        return execution_id;
    }

    public void setExecution_id(int execution_id) {
        this.execution_id = execution_id;
    }

    public String getData_path() {
        return data_path;
    }

    public void setData_path(String data_path) {
        this.data_path = data_path;
    }

    public String getData_type() {
        return data_type;
    }

    public void setData_type(String data_type) {
        this.data_type = data_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShort_description() {
        return short_description;
    }

    public void setShort_description(String short_description) {
        this.short_description = short_description;
    }

    public String getResource_storage_type() {
        return resource_storage_type;
    }

    public void setResource_storage_type(String resource_storage_type) {
        this.resource_storage_type = resource_storage_type;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public Integer[] getCoord_sys() {
        return coord_sys;
    }

    public void setCoord_sys(Integer[] coord_sys) {
        this.coord_sys = coord_sys;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    /*public String getWkb_geometry() {
        GeoJsonWriter writer = new GeoJsonWriter();
        return writer.write(wkb_geometry);
    }

    public void setWkb_geometry(String geometryAsJson) {
        GeoJsonReader reader = new GeoJsonReader();
        try {
            wkb_geometry = reader.read(geometryAsJson);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }*/

    public String getWkb_geometry() {
        GeometryJSON g = new GeometryJSON();
        return g.toString(wkb_geometry);
    }

    public void setWkb_geometry(String geometryAsJson) {
        GeometryJSON g = new GeometryJSON();
        try {
            wkb_geometry = g.read(geometryAsJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public boolean isManaged_resource_storage() {
        return managed_resource_storage;
    }

    public void setManaged_resource_storage(boolean managed_resource_storage) {
        this.managed_resource_storage = managed_resource_storage;
    }

    public String getEntry_point() {
        return entry_point;
    }

    public void setEntry_point(String entry_point) {
        this.entry_point = entry_point;
    }
}
