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
package ro.cs.tao.utils.executors;

/**
 * Possible SSH connection modes
 *
 * @author Cosmin Cara
 */
public enum SSHMode {
    SESSION("session"),
    SHELL("shell"),
    EXEC("exec"),
    X11("x11"),
    AGENT_FORWARDING("auth-agent@openssh.com"),
    DIRECT_TCPIP("direct-tcpip"),
    FORWARDED_TCPIP("forwarded-tcpip"),
    SFTP("sftp"),
    SUBSYSTEM("subsystem");

    private final String text;

    private SSHMode(final String text) { this.text = text; }

    @Override
    public String toString() { return this.text; }

    public boolean equals(SSHMode other) {
        return other != null && other.toString().equals(this.text);
    }
}
