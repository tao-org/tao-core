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
package ro.cs.tao.utils.executors;

import ro.cs.tao.utils.ExecutionUnitFormat;
import ro.cs.tao.utils.executors.container.ContainerType;
import ro.cs.tao.utils.executors.container.ContainerUnit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Structure holding information for a command to be executed
 *
 * @author Cosmin Cara
 */
public class ExecutionUnit {
    protected final ExecutorType type;
    protected ExecutionUnitFormat unitFormat;
    protected final String host;
    protected final String user;
    protected final String password;
    protected String certificate;
    protected final List<String> arguments;
    protected String workingDirectory;
    protected final boolean asSuperUser;
    protected final SSHMode sshMode;
    protected Long minMemory;
    protected Long minDisk;
    protected ContainerUnit containerUnit;
    protected Map<String, Object> metadata;
    protected final boolean asyncSSHExecution;
    protected final String asyncSSHFileName;

    public ExecutionUnit(ExecutorType type, String host, String user, String password, List<String> arguments, boolean asSuperUser, SSHMode sshMode) {
    	this(type, host, user, password, arguments, asSuperUser, sshMode, false, null, ExecutionUnitFormat.TAO);
    }

    public ExecutionUnit(ExecutorType type, String host, String user, String password, List<String> arguments, boolean asSuperUser, SSHMode sshMode, ExecutionUnitFormat format) {
        this(type, host, user, password, arguments, asSuperUser, sshMode, false, null, format);
    }
    
    public ExecutionUnit(ExecutorType type, String host, String user, String password, List<String> arguments, boolean asSuperUser, SSHMode sshMode, boolean asyncSSHExecution, String asyncSSHFileName, ExecutionUnitFormat format) {
        this.type = type;
        this.host = host;
        this.user = user;
        this.password = password;
        this.arguments = arguments;
        this.asSuperUser = asSuperUser;
        this.sshMode = sshMode;
        this.asyncSSHExecution = asyncSSHExecution;
        this.asyncSSHFileName = asyncSSHFileName;
        this.unitFormat = format;
    }

    public ExecutorType getType() { return type; }

    public ExecutionUnitFormat getUnitFormat() {
        return unitFormat;
    }

    public void setUnitFormat(ExecutionUnitFormat unitFormat) {
        this.unitFormat = unitFormat;
    }

    public String getHost() { return host; }

    public List<String> getArguments() { return arguments; }

    public boolean asSuperUser() { return asSuperUser; }

    public SSHMode getSshMode() { return sshMode; }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getCertificate() {
        return certificate;
    }

    public Long getMinMemory() {
        return minMemory;
    }

    public void setMinMemory(Long minMemory) {
        this.minMemory = minMemory;
    }

    public Long getMinDisk() { return minDisk; }

    public void setMinDisk(Long minDisk) { this.minDisk = minDisk; }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public void setContainerType(ContainerType type) {
        this.containerUnit = new ContainerUnit(type);
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workdingDirectory) {
        this.workingDirectory = workdingDirectory;
    }

    public boolean hasContainerArguments() {
        return this.containerUnit != null;
    }

    public void addContainerVolumeMapping(String source, String target) {
        containerUnit().addVolumeMapping(source, target);
    }

    public void addContainerEnvironmentVariable(String name, String value) {
        containerUnit.addEnvironmentVariable(name, value);
    }

    public void addArgument(String name, String value) {
        containerUnit().addArgument(name, value);
    }

    public Map<String, String> getVolumeMap() {
        return containerUnit().getVolumeMap();
    }

    public List<String> getContainerArguments() {
        return containerUnit().getArguments();
    }

    public Map<String, String> getContainerEnvironmentVariables() {
        return containerUnit().getEnvironmentVariables();
    }

    public ContainerUnit getContainerUnit() {
        return containerUnit;
    }

    public void setContainerUnit(ContainerUnit containerUnit) {
        this.containerUnit = containerUnit;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    public String getScriptTargetPath() {
        return this.metadata != null && this.metadata.containsKey("scriptPath") ? (String) this.metadata.get("scriptPath") : null;
    }

    @Override
    public String toString() {
        return "ExecutionUnit {" +
                "host='" + host + '\'' +
                ", arguments=" + (arguments != null ? "'" + String.join(",", arguments) + "'" : "none") + "}";
    }

    private ContainerUnit containerUnit() {
        if (this.containerUnit == null) {
            throw new IllegalArgumentException("Container type not set");
        }
        return this.containerUnit;
    }

	public boolean isAsyncSSHExecution() {
		return asyncSSHExecution;
	}

	public String getAsyncSSHFileName() {
		return asyncSSHFileName;
	}
}
