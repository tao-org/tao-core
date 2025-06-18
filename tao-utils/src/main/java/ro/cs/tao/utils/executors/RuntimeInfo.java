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

import org.apache.commons.lang3.SystemUtils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Abstract class representing runtime information of a system.
 * Provides methods to retrieve details about system resources such as
 * CPU usage, memory, and disk usage.
 */
public abstract class RuntimeInfo {

    final String host;
    final String user;
    final String pass;
    final AuthenticationType authenticationType;
    final boolean isRemote;

    RuntimeInfo(String host, AuthenticationType authenticationType, String user, String password, boolean remote) {
        this.host = host;
        this.user = user;
        this.pass = password;
        this.authenticationType = authenticationType;
        this.isRemote = remote;
    }

    /**
     * Creates an instance of {@code RuntimeInfo} tailored to the specified host and operating system.
     * Depending on whether the hostname matches the local host or not, and the detected operating system,
     * the method returns a specific implementation of {@code RuntimeInfo}.
     *
     * @param hostName the name of the host where the inspector will be created. If it matches the local
     *                 host, the implementation created is specific to the current operating system.
     * @param authenticationType the authentication mechanism to use, e.g., password-based or certificate-based.
     * @param user the username to authenticate with.
     * @param password the password to authenticate with.
     * @return a {@code RuntimeInfo} instance, which can either be for Windows or Linux, and either local or remote.
     * @throws Exception if an error occurs during the creation of the inspector, including network issues or
     *                   incorrect configurations.
     */
    public static RuntimeInfo createInspector(String hostName, AuthenticationType authenticationType,
                                              String user, String password) throws Exception {
        String localhost = InetAddress.getLocalHost().getHostName();
        if (localhost.equals(hostName)) {
            return SystemUtils.IS_OS_WINDOWS
                   ? new Windows(hostName, authenticationType, user, password, false)
                   : new Linux(hostName, authenticationType, user, password, false);
        } else {
            return new Linux(hostName, authenticationType, user, password, true);
        }
    }

    /**
     * Retrieves the current processor usage in percentage.
     * The method is abstract and the actual implementation may vary,
     * depending on whether the processor usage is being measured on a
     * local or remote machine and the operating system.
     *
     * @return the current processor usage as a percentage value, typically
     *         in the range of 0.0 to 100.0, where 0.0 indicates no CPU usage
     *         and 100.0 indicates full CPU usage.
     */
    public abstract double getProcessorUsage();

    /**
     * Retrieves the total memory available on the system in megabytes.
     * This method is abstract, and its implementation may vary depending
     * on the platform and whether the information is being collected
     * from a local or remote machine.
     *
     * @return the total memory in megabytes, representing the full amount
     *         of physical memory installed in the system.
     */
    public abstract long getTotalMemoryMB();

    /**
     * Retrieves the available memory in the system in megabytes.
     * The method is abstract, and the implementation may vary depending
     * on whether the information is being gathered from a local or remote
     * system and on the type of operating system.
     *
     * @return the amount of available memory in megabytes, representing
     *         the memory currently unallocated and available for use.
     */
    public abstract long getAvailableMemoryMB();

    /**
     * Retrieves the total disk space available on the system in gigabytes.
     * This method is abstract, and its implementation may vary depending on
     * whether the information is being gathered from a local or remote system
     * and the type of operating system.
     *
     * @return the total disk space in gigabytes, representing the full capacity
     *         of the storage available on the system.
     */
    public abstract long getTotalDiskGB();

    /**
     * Retrieves the used disk space on the system in gigabytes.
     * This method is abstract, and its implementation may vary depending
     * on whether the information is being gathered from a local or remote
     * machine and the type of operating system.
     *
     * @return the used disk space in gigabytes, representing the amount of
     *         storage currently occupied on the system.
     */
    public abstract long getUsedDiskGB();

    /**
     * The Windows class extends the RuntimeInfo class and provides implementations
     * of methods to retrieve resource usage details in a Windows operating system.
     * It utilizes the Windows Management Instrumentation Command-line (WMIC) tool
     * to fetch system metrics such as CPU usage, memory utilization, and disk
     * information, executing these commands on the host machine.
     */
    private static class Windows extends RuntimeInfo {

        Windows(String host, AuthenticationType authenticationType, String user, String password,boolean remote) {
            super(host, authenticationType, user, password, remote);
        }

        @Override
        public double getProcessorUsage() {
            double cpu = 0.0;
            try {
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "wmic cpu get loadpercentage /value".split(" "));
                Executor executor = Executor.create(ExecutorType.PROCESS, this.host, args);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("LoadPercentage")) {
                            cpu = Double.parseDouble(message.split("=")[1]);
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
            }
            return cpu;
        }

        @Override
        public long getTotalMemoryMB() {
            long mem = 0;
            try {
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "wmic os get totalvisiblememorysize /value".split(" "));
                Executor executor = Executor.create(ExecutorType.PROCESS, this.host, args);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("TotalVisibleMemorySize")) {
                            mem = Long.parseLong(message.split("=")[1]) / MemoryUnit.KB.value();
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
            }
            return mem;
        }

        @Override
        public long getAvailableMemoryMB() {
            long mem = 0;
            try {
                List<String> args = new ArrayList<>();
                //Collections.addAll(args, "wmic os get freephysicalmemory /value".split(" "));
                Collections.addAll(args, "wmic os get FreePhysicalMemory /value".split(" "));
                Executor executor = Executor.create(ExecutorType.PROCESS, this.host, args);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("FreePhysicalMemory")) {
                            // WMIC returns total memory in kilobytes
                            mem = Long.parseLong(message.split("=")[1]) / MemoryUnit.KB.value();
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
            }
            return mem;
        }

        @Override
        public long getTotalDiskGB() {
            long mem = 0;
            try {
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "wmic logicaldisk get size /value".split(" "));
                Executor executor = Executor.create(ExecutorType.PROCESS, this.host, args);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("Size") && message.length() > 5) {
                            String strSize = message.split("=")[1];
                            if (strSize != null && !strSize.isEmpty()) {
                                mem += Long.parseLong(strSize);
                            }
                        }
                    }
                    // WMIC returns total disk in bytes
                    mem = mem / MemoryUnit.GB.value();
                }
            } catch (Exception ex) {
                Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
            }
            return mem;
        }

        @Override
        public long getUsedDiskGB() {
            long mem = 0;
            try {
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "wmic logicaldisk get freespace /value".split(" "));
                Executor executor = Executor.create(ExecutorType.PROCESS, this.host, args);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("FreeSpace=") && message.length() > 10) {
                            String strSize = message.split("=")[1];
                            if (strSize != null && !strSize.isEmpty()) {
                                mem += Long.parseLong(strSize);
                            }
                        }
                    }
                    long total = getTotalDiskGB();
                    // WMIC returns disk space in bytes
                    mem = total - mem / MemoryUnit.GB.value();
                }
            } catch (Exception ex) {
                Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
            }
            return mem;
        }
    }

    /**
     * The Linux class extends the RuntimeInfo class and provides specific methods
     * to retrieve system resource information for a Linux host.
     * It supports both local and remote execution depending on the specified configuration.
     *
     * This class uses system commands to fetch various details such as processor usage,
     * total memory, available memory, total disk space, and used disk space.
     * Authentication for remote execution is handled through password or certificate-based authentication.
     */
    private static class Linux extends RuntimeInfo {

        Linux(String host, AuthenticationType authenticationType, String user, String password, boolean remote) {
            super(host, authenticationType, user, password, remote);
        }

        @Override
        public double getProcessorUsage() {
            double cpu = 0.0;
            try {
                Executor executor;
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "uptime".split(" "));
                executor = Executor.create(this.isRemote ? ExecutorType.SSH2 : ExecutorType.PROCESS, this.host, args);
                executor.setUser(this.user);
                switch (authenticationType) {
                    case PASSWORD:
                        executor.setPassword(this.pass);
                        break;
                    case CERTIFICATE:
                        executor.setCertificate(this.pass);
                        break;
                }
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (!message.isEmpty()) {
                            int idx = message.indexOf("load average");
                            String value = message.substring(idx + 14, message.indexOf(",", idx));
                            cpu = Double.parseDouble(value);
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
            }
            return cpu;
        }

        @Override
        public long getTotalMemoryMB() {
            long mem = 0;
            try {
                Executor executor;
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "cat /proc/meminfo".split(" "));
                executor = Executor.create(this.isRemote ? ExecutorType.SSH2 : ExecutorType.PROCESS, this.host, args);
                executor.setUser(this.user);
                switch (authenticationType) {
                    case PASSWORD:
                        executor.setPassword(this.pass);
                        break;
                    case CERTIFICATE:
                        executor.setCertificate(this.pass);
                        break;
                }
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("MemTotal")) {
                            message = message.replace("MemTotal:", "").trim();
                            String value = message.substring(0, message.indexOf(" kB"));
                            mem = Long.parseLong(value) / MemoryUnit.KB.value();
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
            }
            return mem;
        }

        @Override
        public long getAvailableMemoryMB() {
            long mem = 0;
            try {
                Executor executor;
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "cat /proc/meminfo".split(" "));
                executor = Executor.create(this.isRemote ? ExecutorType.SSH2 : ExecutorType.PROCESS, this.host, args);
                executor.setUser(this.user);
                switch (authenticationType) {
                    case PASSWORD:
                        executor.setPassword(this.pass);
                        break;
                    case CERTIFICATE:
                        executor.setCertificate(this.pass);
                        break;
                }
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    if (messages.isEmpty()) {
                        Logger.getLogger(RuntimeInfo.class.getName()).warning("Cannot capture output for cat /proc/meminfo");
                    }
                    for (String message : messages) {
                        if (message.startsWith("MemAvailable")) {
                            message = message.replace("MemAvailable:", "").trim();
                            String value = message.substring(0, message.indexOf(" kB"));
                            mem = Long.parseLong(value) / MemoryUnit.KB.value();
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
            }
            return mem;
        }

        @Override
        public long getTotalDiskGB() {
            long mem = 0;
            try {
                Executor executor;
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "df -k --total".split(" "));
                executor = Executor.create(this.isRemote ? ExecutorType.SSH2 : ExecutorType.PROCESS, this.host, args);
                executor.setUser(this.user);
                switch (authenticationType) {
                    case PASSWORD:
                        executor.setPassword(this.pass);
                        break;
                    case CERTIFICATE:
                        executor.setCertificate(this.pass);
                        break;
                }
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("total")) {
                            String[] values = message.replace("total", "").trim().split(" ");
                            mem = Long.parseLong(values[0]) / MemoryUnit.MB.value();
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
            }
            return mem;
        }

        @Override
        public long getUsedDiskGB() {
            long mem = 0;
            try {
                Executor executor;
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "df -k --total".split(" "));
                executor = Executor.create(this.isRemote ? ExecutorType.SSH2 : ExecutorType.PROCESS, this.host, args);
                executor.setUser(this.user);
                switch (authenticationType) {
                    case PASSWORD:
                        executor.setPassword(this.pass);
                        break;
                    case CERTIFICATE:
                        executor.setCertificate(this.pass);
                        break;
                }
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("total")) {
                            String[] values = message.replace("total", "").trim().split(" ");
                            mem = Long.parseLong(values[1]) / MemoryUnit.MB.value();
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
            }
            return mem;
        }
    }

    private static class Consumer implements OutputConsumer {
        private final List<String> messages = new ArrayList<>();
        @Override
        public void consume(String message) {
            messages.add(message.replace("\r", ""));
        }
        List<String> getMessages() { return this.messages; }
    }

}
