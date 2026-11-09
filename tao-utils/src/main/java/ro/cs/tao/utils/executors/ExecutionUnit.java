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

    /**
     * Retrieves the executor type associated with this execution unit.
     *
     * @return the executor type, represented as an {@link ExecutorType}.
     */
    public ExecutorType getType() { return type; }

    public ExecutionUnitFormat getUnitFormat() {
        return unitFormat;
    }

    /**
     * Sets the format in which the execution unit should be processed.
     *
     * @param unitFormat the desired execution unit format; an instance of {@link ExecutionUnitFormat}.
     */
    public void setUnitFormat(ExecutionUnitFormat unitFormat) {
        this.unitFormat = unitFormat;
    }

    /**
     * Retrieves the host associated with this execution unit.
     *
     * @return the host name or IP address as a {@code String}.
     */
    public String getHost() { return host; }

    /**
     * Retrieves the list of arguments associated with this execution unit.
     *
     * @return a list of arguments, represented as {@code List<String>}.
     */
    public List<String> getArguments() { return arguments; }

    /**
     * Determines if the execution unit is configured to run with superuser privileges.
     *
     * @return {@code true} if the execution unit is set to execute as a superuser; {@code false} otherwise.
     */
    public boolean asSuperUser() { return asSuperUser; }

    /**
     * Retrieves the SSH connection mode configured for this execution unit.
     *
     * @return the configured SSH connection mode, represented as an {@link SSHMode} enum.
     */
    public SSHMode getSshMode() { return sshMode; }

    /**
     * Retrieves the username associated with this execution unit.
     *
     * @return the username as a {@code String}.
     */
    public String getUser() {
        return user;
    }

    /**
     * Retrieves the password associated with this execution unit.
     *
     * @return the password as a {@code String}.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Retrieves the certificate associated with this execution unit.
     *
     * @return the certificate as a {@code String}.
     */
    public String getCertificate() {
        return certificate;
    }

    /**
     * Retrieves the minimum memory requirement for the execution unit.
     *
     * @return the minimum memory, represented as a {@code Long}.
     */
    public Long getMinMemory() {
        return minMemory;
    }

    /**
     * Sets the minimum memory requirement for the execution unit.
     *
     * @param minMemory the minimum memory, represented as a {@code Long}.
     */
    public void setMinMemory(Long minMemory) {
        this.minMemory = minMemory;
    }

    /**
     * Retrieves the minimum disk space requirement for the execution unit.
     *
     * @return the minimum disk space, represented as a {@code Long}.
     */
    public Long getMinDisk() { return minDisk; }

    /**
     * Sets the minimum disk space requirement for the execution unit.
     *
     * @param minDisk the minimum disk space, represented as a {@code Long}.
     */
    public void setMinDisk(Long minDisk) { this.minDisk = minDisk; }

    /**
     * Sets the certificate associated with this execution unit.
     *
     * @param certificate the certificate represented as a {@code String}.
     */
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    /**
     * Sets the container type for the execution unit by creating a new {@link ContainerUnit}
     * with the specified type.
     *
     * @param type the desired container type, represented as a {@link ContainerType}
     *             (e.g., {@code ContainerType.DOCKER} or {@code ContainerType.KUBERNETES}).
     */
    public void setContainerType(ContainerType type) {
        this.containerUnit = new ContainerUnit(type);
    }

    /**
     * Retrieves the working directory associated with this execution unit.
     *
     * @return the working directory as a {@code String}.
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Sets the working directory for this execution unit.
     *
     * @param workingDirectory the path to the desired working directory, represented as a {@code String}.
     */
    public void setWorkingDirectory(String workdingDirectory) {
        this.workingDirectory = workdingDirectory;
    }

    /**
     * Determines if container arguments are present for the execution unit.
     *
     * @return {@code true} if the execution unit has a non-null container unit; {@code false} otherwise.
     */
    public boolean hasContainerArguments() {
        return this.containerUnit != null;
    }

    /**
     * Adds a volume mapping for the container associated with this execution unit.
     * The volume mapping specifies the source path on the host and the target path within the container.
     *
     * @param source the source path on the host system to be mapped, represented as a {@code String}.
     * @param target the target path within the container, represented as a {@code String}.
     * @throws IllegalArgumentException if the container type is not set.
     */
    public void addContainerVolumeMapping(String source, String target) {
        containerUnit().addVolumeMapping(source, target);
    }

    /**
     * Adds an environment variable to the container associated with this execution unit.
     *
     * @param name the name of the environment variable, represented as a {@code String}.
     * @param value the value of the environment variable, represented as a {@code String}.
     */
    public void addContainerEnvironmentVariable(String name, String value) {
        containerUnit.addEnvironmentVariable(name, value);
    }

    /**
     * Adds an argument to the container unit of the execution unit.
     *
     * @param name  the name of the argument, represented as a {@code String}.
     * @param value the value of the argument, represented as a {@code String}.
     * @throws IllegalArgumentException if the container type is not set.
     */
    public void addArgument(String name, String value) {
        containerUnit().addArgument(name, value);
    }

    /**
     * Retrieves the volume mappings associated with the container of this execution unit.
     * The mappings define the relationship between source paths on the host
     * and target paths within the container.
     *
     * @return a map containing the volume mappings, where the keys represent the source paths
     *         on the host system and the values represent the corresponding target paths
     *         within the container.
     * @throws IllegalArgumentException if the container type is not set.
     */
    public Map<String, String> getVolumeMap() {
        return containerUnit().getVolumeMap();
    }

    /**
     * Retrieves the list of arguments associated with the container unit of this execution unit.
     *
     * @return a list of container-specific arguments, represented as {@code List<String>}.
     * @throws IllegalArgumentException if the container type is not set.
     */
    public List<String> getContainerArguments() {
        return containerUnit().getArguments();
    }

    /**
     * Retrieves the environment variables associated with the container of this execution unit.
     *
     * @return a map containing environment variables, where the keys represent the names
     *         of the variables and the values represent their corresponding values.
     * @throws IllegalArgumentException if the container type is not set.
     */
    public Map<String, String> getContainerEnvironmentVariables() {
        return containerUnit().getEnvironmentVariables();
    }

    /**
     * Retrieves the container unit associated with this execution unit.
     *
     * @return the associated {@link ContainerUnit}, or {@code null} if no container unit is set.
     */
    public ContainerUnit getContainerUnit() {
        return containerUnit;
    }

    /**
     * Sets the container unit for this execution unit.
     *
     * @param containerUnit the container unit to be set, represented as an instance of {@link ContainerUnit}.
     */
    public void setContainerUnit(ContainerUnit containerUnit) {
        this.containerUnit = containerUnit;
    }

    /**
     * Retrieves metadata associated with this execution unit.
     *
     * @return a map containing the metadata, where keys are represented as {@code String}
     *         and values are represented as {@code Object}.
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Adds a metadata key-value pair to this execution unit. If the metadata map is not initialized,
     * it will be created.
     *
     * @param key the metadata key to add, represented as a {@code String}.
     * @param value the metadata value to associate with the key, represented as an {@code Object}.
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    /**
     * Retrieves the script target path from the metadata of the execution unit.
     * If the "scriptPath" key exists in the metadata map, its corresponding value
     * is returned. Otherwise, null is returned.
     *
     * @return the script target path as a {@code String} if available;
     *         otherwise, {@code null}.
     */
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

    /**
     * Determines if the SSH execution for this execution unit is configured to run asynchronously.
     *
     * @return {@code true} if the SSH execution is set to run asynchronously; {@code false} otherwise.
     */
	public boolean isAsyncSSHExecution() {
		return asyncSSHExecution;
	}

    /**
     * Retrieves the file name associated with asynchronous SSH execution for this execution unit.
     *
     * @return the asynchronous SSH file name as a {@code String}.
     */
	public String getAsyncSSHFileName() {
		return asyncSSHFileName;
	}
}
