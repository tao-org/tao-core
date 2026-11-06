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

package ro.cs.tao.execution.monitor;

import ro.cs.tao.utils.executors.AuthenticationType;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * The OSRuntimeInfo class represents a blueprint for monitoring operating system metrics at runtime.
 * It is an abstract class and serves as a base for specific implementations for various operating systems.
 * This class provides methods to retrieve system-level details such as processor usage, memory, and disk space
 * and allows obtaining runtime snapshots of the system state.
 *
 * @param <R> The type of the object used to represent detailed runtime information.
 */
public abstract class OSRuntimeInfo<R extends RuntimeInfo> {

    final String node;
    final String user;
    final String password;
    final boolean isRemote;
    final Class<R> runtimeClass;
    final AuthenticationType authenticationType;
    final NodeRuntimeInspector inspector;
    final Logger logger;

    OSRuntimeInfo(String host, AuthenticationType authenticationType, String user, String password, boolean remote, Class<R> clazz) {
        this.node = host;
        this.user = user;
        this.authenticationType = authenticationType;
        this.password = password;
        this.isRemote = remote;
        this.runtimeClass = clazz;
        this.inspector = createInspector(host, authenticationType, user, password, remote);
        this.logger = Logger.getLogger(getClass().getName());
    }

    /**
     * Retrieves the current CPU usage percentage.
     *
     * @return the percentage of processor usage as a double value.
     * @throws IOException if an I/O error occurs during the retrieval of processor usage.
     */
    public double getProcessorUsage() throws IOException {
        return this.inspector.getProcessorUsage();
    }

    /**
     * Retrieves the total memory available on the system in megabytes.
     *
     * @return the total memory in megabytes as a long value.
     * @throws IOException if an I/O error occurs during the retrieval process.
     */
    public long getTotalMemoryMB() throws IOException {
        return this.inspector.getTotalMemoryMB();
    }

    /**
     * Retrieves the currently available memory on the system in megabytes.
     *
     * @return the available memory in megabytes as a long value
     * @throws IOException if an I/O error occurs during the retrieval of available memory
     */
    public long getAvailableMemoryMB() throws IOException {
        return this.inspector.getAvailableMemoryMB();
    }

    /**
     * Retrieves the total disk space available on the system in gigabytes.
     *
     * @return the total disk space in gigabytes as a long value
     * @throws IOException if an I/O error occurs during the retrieval of disk space
     */
    public long getTotalDiskGB() throws IOException {
        return this.inspector.getTotalDiskGB();
    }

    /**
     * Retrieves the currently used disk space on the system in gigabytes.
     *
     * @return the used disk space in gigabytes as a long value
     * @throws IOException if an I/O error occurs during the retrieval of disk space
     */
    public long getUsedDiskGB() throws IOException {
        return this.inspector.getUsedDiskGB();
    }

    /**
     * Retrieves information encapsulated in the implementation-specific object of type {@code R}.
     * The exact nature of the information depends on the context provided by the implementing class.
     *
     * @return an object of type {@code R} containing the desired information
     * @throws Exception if an error occurs during the information retrieval process
     */
    public R getInfo() throws Exception {
        return this.runtimeClass.cast(this.inspector.getInfo());
    }

    /**
     * Creates and returns a snapshot of the current runtime system information.
     * The snapshot includes CPU usage, total memory, available memory,
     * total disk space, used disk space, and associated memory and disk units.
     *
     * @return an instance of type R that contains the runtime system information
     * @throws Exception if an error occurs during the instantiation or retrieval of system information
     */
    public R getSnapshot() throws Exception {
        return this.runtimeClass.cast(this.inspector.getSnapshot());
    }

    protected abstract NodeRuntimeInspector createInspector(String host, AuthenticationType authenticationType, String user, String password, boolean remote);

    /**
     * The Windows class is an implementation of {@link OSRuntimeInfo} specifically
     * designed for gathering system and hardware runtime information on Windows-based
     * operating systems. It retrieves details such as CPU usage, memory availability,
     * and disk usage by executing WMIC commands and parsing their output.
     *
     * @param <R> A type parameter extending {@link RuntimeInfo} that represents the
     *            structure holding runtime data for the Windows operating system.
     */
    private static class Windows<R extends RuntimeInfo> extends OSRuntimeInfo<R> {

        Windows(String host, String user, String password, boolean remote, AuthenticationType authType, Class<R> clazz) {
            super(host, authType, user, password, remote, clazz);
        }

        @Override
        protected NodeRuntimeInspector createInspector(String host, AuthenticationType authenticationType, String user, String password, boolean remote) {
            return new WindowsRuntimeInspector(host, authenticationType, user, password, remote);
        }
    }

    /**
     * Represents a Linux-based operating system runtime information retriever.
     * Provides functionality for retrieving system metrics such as CPU usage,
     * memory, and disk information from a Linux-based system, either locally
     * or remotely using SSH.
     *
     * @param <R> a subclass of RuntimeInfo representing the runtime information structure
     */
    public static class Linux<R extends RuntimeInfo> extends OSRuntimeInfo<R> {

        public Linux(String host, String user, String password, boolean remote, Class<R> clazz) {
            this(host, AuthenticationType.PASSWORD, user, password, remote, clazz);
        }

        public Linux(String host, AuthenticationType authenticationType, String user, String authenticationToken, boolean remote, Class<R> clazz) {
            super(host, authenticationType, user, authenticationToken, remote, clazz);
        }

        @Override
        protected NodeRuntimeInspector createInspector(String host, AuthenticationType authenticationType, String user, String password, boolean remote) {
            return new LinuxRuntimeInspector(host, authenticationType, user, password, remote);
        }
    }
}
