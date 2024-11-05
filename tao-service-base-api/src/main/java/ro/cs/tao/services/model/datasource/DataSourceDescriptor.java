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
package ro.cs.tao.services.model.datasource;

import ro.cs.tao.datasource.param.DataSourceParameter;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class DataSourceDescriptor {
    private String mission;
    private String sensor;
    private String dataSourceName;
    private String category;
    private String description;
	private String temporalCoverage;
	private String spatialCoverage;
    private String user;
    private String pwd;
    private String secret;
    private Map<String, DataSourceParameter> parameters;
    private boolean requiresAuthentication;
    private boolean requires2FA;
    private boolean requiresBearerToken;

    public DataSourceDescriptor() {
    }

    public DataSourceDescriptor(String mission, String sensor, String dataSourceName, String category, String description,
								String temporalCoverage, String spatialCoverage,
								Map<String, DataSourceParameter> parameters,
                                boolean requiresAuthentication, boolean requires2FA, boolean requiresBearerToken) {
        this.mission = mission;
        this.sensor = sensor;
        this.dataSourceName = dataSourceName;
        this.parameters = parameters;
		this.category = category;
		this.description = description;
		this.temporalCoverage = temporalCoverage;
		this.spatialCoverage = spatialCoverage;
        this.requiresAuthentication = requiresAuthentication;
        this.requires2FA = requires2FA;
        this.requiresBearerToken = requiresBearerToken;
        if (this.parameters != null) {
            Iterator<String> iterator = parameters.keySet().iterator();
            int order = 1;
            while (iterator.hasNext()) {
                parameters.get(iterator.next()).setOrder(order++);
            }
        }
    }

    public String getMission() {
        return mission;
    }

    public void setMission(String mission) {
        this.mission = mission;
    }

    public String getSensor() {
        return sensor;
    }
    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemporalCoverage() { return temporalCoverage; }

    public void setTemporalCoverage(String temporalCoverage) { this.temporalCoverage = temporalCoverage; }

    public String getSpatialCoverage() { return spatialCoverage; }

    public void setSpatialCoverage(String spatialCoverage) { this.spatialCoverage = spatialCoverage; }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public boolean isRequiresAuthentication() {
        return requiresAuthentication;
    }

    public void setRequiresAuthentication(boolean requiresAuthentication) {
        this.requiresAuthentication = requiresAuthentication;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean isRequires2FA() {
        return requires2FA;
    }

    public void setRequires2FA(boolean requires2FA) {
        this.requires2FA = requires2FA;
    }

    public boolean isRequiresBearerToken() {
        return requiresBearerToken;
    }

    public void setRequiresBearerToken(boolean requiresBearerToken) {
        this.requiresBearerToken = requiresBearerToken;
    }

    public Map<String, DataSourceParameter> getParameters() {
        return parameters;
    }
    public void setParameters(Map<String, DataSourceParameter> parameters) {
        this.parameters = parameters;
    }
}
