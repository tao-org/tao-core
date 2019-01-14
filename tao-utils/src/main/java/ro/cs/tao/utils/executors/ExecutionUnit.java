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

import java.util.List;

/**
 * Structure holding information for a command to be executed
 *
 * @author Cosmin Cara
 */
public final class ExecutionUnit {
    private final ExecutorType type;
    private final String host;
    private final String user;
    private final String password;
    private final List<String> arguments;
    private final boolean asSuperUser;
    private final SSHMode sshMode;
    private Long minMemory;
    private Long minDisk;

    public ExecutionUnit(ExecutorType type, String host, String user, String password, List<String> arguments, boolean asSuperUser, SSHMode sshMode) {
        this.type = type;
        this.host = host;
        this.user = user;
        this.password = password;
        this.arguments = arguments;
        this.asSuperUser = asSuperUser;
        this.sshMode = sshMode;
    }

    public ExecutorType getType() { return type; }

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

    public Long getMinMemory() {
        return minMemory;
    }

    public void setMinMemory(Long minMemory) {
        this.minMemory = minMemory;
    }

    public Long getMinDisk() { return minDisk; }

    public void setMinDisk(Long minDisk) { this.minDisk = minDisk; }

    @Override
    public String toString() {
        return "ExecutionUnit {" +
                "host='" + host + '\'' +
                ", arguments=" + (arguments != null ? "'" + String.join(",", arguments) + "'" : "none") + "}";
    }
}
