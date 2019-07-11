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

import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.utils.executors.Executor;
import ro.cs.tao.utils.executors.ExecutorType;
import ro.cs.tao.utils.executors.OutputConsumer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public abstract class OSRuntimeInfo {

    final String node;
    final String user;
    final String password;
    final boolean isRemote;
    final Logger logger;

    OSRuntimeInfo(String host, String user, String password, boolean remote) {
        this.node = host;
        this.user = user;
        this.password = password;
        this.isRemote = remote;
        this.logger = Logger.getLogger(getClass().getName());
    }

    public static OSRuntimeInfo createInspector(String host, String user, String password) throws Exception {
        String localhost = InetAddress.getLocalHost().getHostName();
        if (localhost.equals(host)) {
            return SystemUtils.IS_OS_WINDOWS ?
                    new Windows(host, user, password, false) : new Linux(host, user, password, false);
        } else {
            return new Linux(host, user, password, true);
        }
    }

    public abstract double getProcessorUsage() throws IOException;
    public abstract long getTotalMemoryMB() throws IOException;
    public abstract long getAvailableMemoryMB() throws IOException;
    public abstract long getTotalDiskGB() throws IOException;
    public abstract long getUsedDiskGB() throws IOException;
    public abstract RuntimeInfo getInfo() throws IOException;

    public RuntimeInfo getSnapshot() throws IOException {
        RuntimeInfo runtimeInfo = new RuntimeInfo();
        runtimeInfo.setCpuTotal(getProcessorUsage());
        runtimeInfo.setTotalMemory(getTotalMemoryMB());
        runtimeInfo.setAvailableMemory(getAvailableMemoryMB());
        runtimeInfo.setDiskTotal(getTotalDiskGB());
        runtimeInfo.setDiskUsed(getUsedDiskGB());
        runtimeInfo.setDiskUnit(MemoryUnit.GIGABYTE);
        runtimeInfo.setMemoryUnit(MemoryUnit.MEGABYTE);
        return runtimeInfo;
    }

    private static class Windows extends OSRuntimeInfo {

        Windows(String host, String user, String password, boolean remote) {
            super(host, user, password, remote);
        }

        @Override
        public RuntimeInfo getInfo() throws IOException {
            RuntimeInfo runtimeInfo = new RuntimeInfo();
            List<String> args = new ArrayList<>();
            Collections.addAll(args, "cmd", "/c");
            args.add("wmic cpu get loadpercentage /value && " +
                             "wmic os get totalvisiblememorysize /value && " +
                             "wmic os get freephysicalmemory /value && " +
                             "wmic logicaldisk get size /value && " +
                             "wmic logicaldisk get freespace /value");
            Executor executor = Executor.create(ExecutorType.PROCESS, this.node, args);
            Consumer consumer = new Consumer();
            executor.setOutputConsumer(consumer);
            try {
                long diskSize = 0, freeSpace = 0;
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        String[] strings = message.split("=");
                        if (strings.length == 1) {
                            strings = new String[] { message, "0" };
                        }
                        if (message.startsWith("LoadPercentage")) {
                            runtimeInfo.setCpuTotal(Double.parseDouble(strings[1]));
                        } else if (message.startsWith("TotalVisibleMemorySize")) {
                            runtimeInfo.setTotalMemory(Long.parseLong(strings[1]) / MemoryUnit.KILOBYTE.value());
                            runtimeInfo.setMemoryUnit(MemoryUnit.MEGABYTE);
                        } else if (message.startsWith("FreePhysicalMemory")) {
                            // WMIC returns total memory in kilobytes
                            runtimeInfo.setAvailableMemory(Long.parseLong(strings[1]) / MemoryUnit.KILOBYTE.value());
                        } else if (message.startsWith("Size") && message.length() > 5) {
                            String strSize = strings[1];
                            if (strSize != null && !strSize.isEmpty()) {
                                diskSize += Long.parseLong(strSize);
                            }
                        } else if (message.startsWith("FreeSpace=") && message.length() > 10) {
                            String strSize = strings[1];
                            if (strSize != null && !strSize.isEmpty()) {
                                freeSpace += Long.parseLong(strSize);
                            }
                        }
                    }
                    // WMIC returns total disk in bytes
                    diskSize = diskSize / MemoryUnit.GIGABYTE.value();
                    runtimeInfo.setDiskTotal(diskSize);
                    runtimeInfo.setDiskUsed(diskSize - freeSpace / MemoryUnit.GIGABYTE.value());
                    runtimeInfo.setDiskUnit(MemoryUnit.GIGABYTE);
                }
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
                throw new IOException(ex);
            }
            return runtimeInfo;
        }

        @Override
        public double getProcessorUsage() throws IOException {
            double cpu = 0.0;
            try {
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "wmic cpu get loadpercentage /value".split(" "));
                Executor executor = Executor.create(ExecutorType.PROCESS, this.node, args);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("LoadPercentage")) {
                            String[] strings = message.split("=");
                            if (strings.length == 1) {
                                logger.warning(String.format("Abnormal message: %s", message));
                                continue;
                            }
                            cpu = Double.parseDouble(strings[1]);
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
                throw new IOException(ex);
            }
            return cpu;
        }

        @Override
        public long getTotalMemoryMB() throws IOException {
            long mem = 0;
            try {
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "wmic os get totalvisiblememorysize /value".split(" "));
                Executor executor = Executor.create(ExecutorType.PROCESS, this.node, args);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("TotalVisibleMemorySize")) {
                            String[] strings = message.split("=");
                            if (strings.length == 1) {
                                strings = new String[] { message, "0" };
                            }
                            mem = Long.parseLong(strings[1]) / MemoryUnit.KILOBYTE.value();
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
                throw new IOException(ex);
            }
            return mem;
        }

        @Override
        public long getAvailableMemoryMB() throws IOException {
            long mem = 0;
            try {
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "wmic os get freephysicalmemory /value".split(" "));
                Executor executor = Executor.create(ExecutorType.PROCESS, this.node, args);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("FreePhysicalMemory")) {
                            String[] strings = message.split("=");
                            if (strings.length == 1) {
                                strings = new String[] { message, "0" };
                            }
                            // WMIC returns total memory in kilobytes
                            mem = Long.parseLong(strings[1]) / MemoryUnit.KILOBYTE.value();
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
                throw new IOException(ex);
            }
            return mem;
        }

        @Override
        public long getTotalDiskGB() throws IOException {
            long mem = 0;
            try {
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "wmic logicaldisk get size /value".split(" "));
                Executor executor = Executor.create(ExecutorType.PROCESS, this.node, args);
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
                    mem = mem / MemoryUnit.GIGABYTE.value();
                }
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
                throw new IOException(ex);
            }
            return mem;
        }

        @Override
        public long getUsedDiskGB() throws IOException {
            long mem = 0;
            try {
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "wmic logicaldisk get freespace /value".split(" "));
                Executor executor = Executor.create(ExecutorType.PROCESS, this.node, args);
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
                    mem = total - mem / MemoryUnit.GIGABYTE.value();
                }
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
                throw new IOException(ex);
            }
            return mem;
        }
    }

    private static class Linux extends OSRuntimeInfo {

        Linux(String host, String user, String password, boolean remote) {
            super(host, user, password, remote);
        }

        @Override
        public RuntimeInfo getInfo()  throws IOException{
            //return getSnapshot();
            RuntimeInfo runtimeInfo = new RuntimeInfo();
            try {
                Executor executor;
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "uptime && cat /proc/meminfo && df -k --total".split(" "));
                executor = Executor.create(this.isRemote ? ExecutorType.SSH2 : ExecutorType.PROCESS, this.node, args);
                executor.setUser(this.user);
                executor.setPassword(this.password);
                final Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                final int code = executor.execute(false);
                if (code == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (!message.isEmpty()) {
                            if (message.startsWith("MemTotal")) {
                                message = message.replace("MemTotal:", "").trim();
                                String value = message.substring(0, message.indexOf(" kB"));
                                runtimeInfo.setTotalMemory(Long.parseLong(value) / MemoryUnit.KILOBYTE.value());
                            } else if (message.startsWith("MemAvailable")) {
                                message = message.replace("MemAvailable:", "").trim();
                                String value = message.substring(0, message.indexOf(" kB"));
                                runtimeInfo.setAvailableMemory(Long.parseLong(value) / MemoryUnit.KILOBYTE.value());
                                break;
                            } else if (message.startsWith("total")) {
                                String[] values = message.replace("total", "").trim().split(" ");
                                runtimeInfo.setDiskTotal(Long.parseLong(values[0]) / MemoryUnit.MEGABYTE.value());
                                runtimeInfo.setDiskUsed(Long.parseLong(values[1]) / MemoryUnit.MEGABYTE.value());
                                break;
                            } else if (message.contains("load average")) {
                                int idx = message.indexOf("load average");
                                String value = message.substring(idx + 14, message.indexOf(",", idx));
                                runtimeInfo.setCpuTotal(Double.parseDouble(value));
                            }
                        }
                    }
                } else {
                    logger.warning(String.format("Could not retrieve node information. Request returned code %d. Output:\n%s",
                                                 code, String.join("\n", consumer.getMessages())));
                }
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
                throw new IOException(ex);
            }
            return runtimeInfo;
        }

        @Override
        public double getProcessorUsage() throws IOException {
            double cpu = 0.0;
            try {
                Executor executor;
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "uptime".split(" "));
                executor = Executor.create(this.isRemote ? ExecutorType.SSH2 : ExecutorType.PROCESS, this.node, args);
                executor.setUser(this.user);
                executor.setPassword(this.password);
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
                logger.severe(ex.getMessage());
                throw new IOException(ex);
            }
            return cpu;
        }

        @Override
        public long getTotalMemoryMB() throws IOException{
            long mem = 0;
            try {
                Executor executor;
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "cat /proc/meminfo".split(" "));
                executor = Executor.create(this.isRemote ? ExecutorType.SSH2 : ExecutorType.PROCESS, this.node, args);
                executor.setUser(this.user);
                executor.setPassword(this.password);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("MemTotal")) {
                            message = message.replace("MemTotal:", "").trim();
                            String value = message.substring(0, message.indexOf(" kB"));
                            mem = Long.parseLong(value) / MemoryUnit.KILOBYTE.value();
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
                throw new IOException(ex);
            }
            return mem;
        }

        @Override
        public long getAvailableMemoryMB() throws IOException {
            long mem = 0;
            try {
                Executor executor;
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "cat /proc/meminfo".split(" "));
                executor = Executor.create(this.isRemote ? ExecutorType.SSH2 : ExecutorType.PROCESS, this.node, args);
                executor.setUser(this.user);
                executor.setPassword(this.password);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("MemAvailable")) {
                            message = message.replace("MemAvailable:", "").trim();
                            String value = message.substring(0, message.indexOf(" kB"));
                            mem = Long.parseLong(value) / MemoryUnit.KILOBYTE.value();
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
                throw new IOException(ex);
            }
            return mem;
        }

        @Override
        public long getTotalDiskGB() throws IOException {
            long mem = 0;
            try {
                Executor executor;
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "df -k --total".split(" "));
                executor = Executor.create(this.isRemote ? ExecutorType.SSH2 : ExecutorType.PROCESS, this.node, args);
                executor.setUser(this.user);
                executor.setPassword(this.password);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("total")) {
                            String[] values = message.replace("total", "").trim().split(" ");
                            mem = Long.parseLong(values[0]) / MemoryUnit.MEGABYTE.value();
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
                throw new IOException(ex);
            }
            return mem;
        }

        @Override
        public long getUsedDiskGB() throws IOException {
            long mem = 0;
            try {
                Executor executor;
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "df -k --total".split(" "));
                executor = Executor.create(this.isRemote ? ExecutorType.SSH2 : ExecutorType.PROCESS, this.node, args);
                executor.setUser(this.user);
                executor.setPassword(this.password);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("total")) {
                            String[] values = message.replace("total", "").trim().split(" ");
                            mem = Long.parseLong(values[1]) / MemoryUnit.MEGABYTE.value();
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                logger.severe(ex.getMessage());
                throw new IOException(ex);
            }
            return mem;
        }
    }

    private static class Consumer implements OutputConsumer {
        private List<String> messages = new ArrayList<>();
        @Override
        public void consume(String message) {
            messages.add(message.replace("\r", ""));
        }
        List<String> getMessages() { return this.messages; }
    }

}
