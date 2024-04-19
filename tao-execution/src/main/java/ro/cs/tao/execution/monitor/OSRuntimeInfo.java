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
import ro.cs.tao.component.SystemVariable;
import ro.cs.tao.utils.executors.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class OSRuntimeInfo<R extends RuntimeInfo> {

    final String node;
    final String user;
    final String password;
    final boolean isRemote;
    final Class<R> runtimeClass;
    final AuthenticationType authenticationType;
    final Logger logger;

    OSRuntimeInfo(String host, AuthenticationType authenticationType, String user, String password, boolean remote, Class<R> clazz) {
        this.node = host;
        this.user = user;
        this.authenticationType = authenticationType;
        this.password = password;
        this.isRemote = remote;
        this.runtimeClass = clazz;
        this.logger = Logger.getLogger(getClass().getName());
    }

    public static <R extends RuntimeInfo> OSRuntimeInfo<R> createInspector(String host, String user, String password,
                                                                           AuthenticationType authType, Class<R> clazz) throws Exception {
        String localhost = InetAddress.getLocalHost().getHostName();
        if (localhost.equals(host)) {
            return SystemUtils.IS_OS_WINDOWS
                   ? new Windows<>(host, user, password, false, authType, clazz)
                   : new Linux<>(host, authType, user, password, false, clazz);
        } else {
            return new Linux<>(host, authType, user, password, true, clazz);
        }
    }

    public abstract double getProcessorUsage() throws IOException;
    public abstract long getTotalMemoryMB() throws IOException;
    public abstract long getAvailableMemoryMB() throws IOException;
    public abstract long getTotalDiskGB() throws IOException;
    public abstract long getUsedDiskGB() throws IOException;
    public abstract R getInfo() throws Exception;

    public R getSnapshot() throws Exception {
        R runtimeInfo = this.runtimeClass.getConstructor().newInstance();
        runtimeInfo.setCpuTotal(getProcessorUsage());
        runtimeInfo.setTotalMemory(getTotalMemoryMB());
        runtimeInfo.setAvailableMemory(getAvailableMemoryMB());
        runtimeInfo.setDiskTotal(getTotalDiskGB());
        runtimeInfo.setDiskUsed(getUsedDiskGB());
        runtimeInfo.setDiskUnit(MemoryUnit.GB);
        runtimeInfo.setMemoryUnit(MemoryUnit.MB);
        return runtimeInfo;
    }

    private static class Windows<R extends RuntimeInfo> extends OSRuntimeInfo<R> {

        Windows(String host, String user, String password, boolean remote, AuthenticationType authType, Class<R> clazz) {
            super(host, authType, user, password, remote, clazz);
        }

        @Override
        public R getInfo() throws Exception {
            R runtimeInfo = this.runtimeClass.getConstructor().newInstance();
            List<String> args = new ArrayList<>();
            Collections.addAll(args, "cmd", "/c");
            args.add("wmic cpu get loadpercentage /value && " +
                             "wmic os get totalvisiblememorysize /value && " +
                             "wmic os get freephysicalmemory /value && " +
                             "wmic logicaldisk get size /value && " +
                             "wmic logicaldisk get freespace /value");
            Executor<?> executor = Executor.create(ExecutorType.PROCESS, this.node, args);
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
                            runtimeInfo.setTotalMemory(Long.parseLong(strings[1]) / MemoryUnit.KB.value());
                            runtimeInfo.setMemoryUnit(MemoryUnit.MB);
                        } else if (message.startsWith("FreePhysicalMemory")) {
                            // WMIC returns total memory in kilobytes
                            runtimeInfo.setAvailableMemory(Long.parseLong(strings[1]) / MemoryUnit.KB.value());
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
                    diskSize = diskSize / MemoryUnit.GB.value();
                    runtimeInfo.setDiskTotal(diskSize);
                    runtimeInfo.setDiskUsed(diskSize - freeSpace / MemoryUnit.GB.value());
                    runtimeInfo.setDiskUnit(MemoryUnit.GB);
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
                Executor<?> executor = Executor.create(ExecutorType.PROCESS, this.node, args);
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
                Executor<?> executor = Executor.create(ExecutorType.PROCESS, this.node, args);
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
                            mem = Long.parseLong(strings[1]) / MemoryUnit.KB.value();
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
                Executor<?> executor = Executor.create(ExecutorType.PROCESS, this.node, args);
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
                            mem = Long.parseLong(strings[1]) / MemoryUnit.KB.value();
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
                Executor<?> executor = Executor.create(ExecutorType.PROCESS, this.node, args);
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
                Executor<?> executor = Executor.create(ExecutorType.PROCESS, this.node, args);
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
                logger.severe(ex.getMessage());
                throw new IOException(ex);
            }
            return mem;
        }
    }

    public static class Linux<R extends RuntimeInfo> extends OSRuntimeInfo<R> {

        public Linux(String host, String user, String password, boolean remote, Class<R> clazz) {
            this(host, AuthenticationType.PASSWORD, user, password, remote, clazz);
        }

        public Linux(String host, AuthenticationType authenticationType, String user, String authenticationToken, boolean remote, Class<R> clazz) {
            super(host, authenticationType, user, authenticationToken, remote, clazz);
        }

        private Executor<?> buildExecutor(List<String> arguments) {
            Executor<?> executor = Executor.create(this.isRemote ? ExecutorType.SSH2 : ExecutorType.PROCESS, this.node, arguments);
            executor.setUser(this.user);
            switch (this.authenticationType) {
                case PASSWORD:
                    executor.setPassword(this.password);
                    break;
                case CERTIFICATE:
                    executor.setCertificate(this.password);
                    break;
            }
            return executor;
        }

        @Override
        public R getInfo() throws Exception {
            R runtimeInfo = this.runtimeClass.getConstructor().newInstance();
            Executor<?> executor = null;
            try {
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "uptime && cat /proc/meminfo && df -k --total".split(" "));
                executor = buildExecutor(args);
                final Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                final int code = executor.execute(false);
                if (code == 0) {
                    List<String> messages = consumer.getMessages();
                    final String mountPointOne = SystemVariable.ROOT.value().substring(0, SystemVariable.ROOT.value().lastIndexOf("/"));
                    final String mountPointTwo = mountPointOne.substring(0, mountPointOne.lastIndexOf("/"));
                    for (String message : messages) {
                        if (!message.isEmpty()) {
                            if (message.startsWith("MemTotal")) {
                                message = message.replace("MemTotal:", "").trim();
                                String value = message.substring(0, message.indexOf(" kB"));
                                runtimeInfo.setTotalMemory(Long.parseLong(value) / MemoryUnit.KB.value());
                            } else if (message.startsWith("MemAvailable")) {
                                message = message.replace("MemAvailable:", "").trim();
                                String value = message.substring(0, message.indexOf(" kB"));
                                runtimeInfo.setAvailableMemory(Long.parseLong(value) / MemoryUnit.KB.value());
                                runtimeInfo.setMemoryUnit(MemoryUnit.KB);
                            } else if (message.startsWith("/dev/mapper/centos-home") ||
                                       message.contains(mountPointOne) || message.contains(mountPointTwo)) {
                                List<String> values = Arrays.stream(message.replace(message.substring(0, message.indexOf(' ')), "").trim().split(" ")).filter(v -> !v.isEmpty()).collect(Collectors.toList());
                                runtimeInfo.setDiskTotal(runtimeInfo.getDiskTotal() + Long.parseLong(values.get(0)) / MemoryUnit.MB.value());
                                runtimeInfo.setDiskUsed(runtimeInfo.getDiskUsed() + Long.parseLong(values.get(1)) / MemoryUnit.MB.value());
                                runtimeInfo.setDiskUnit(MemoryUnit.MB);
                            } else if (message.contains("load average")) {
                                int idx = message.indexOf("load average");
                                String value = message.substring(idx + 14, message.indexOf(",", idx));
                                runtimeInfo.setCpuTotal(Double.parseDouble(value));
                            }
                            if (runtimeInfo instanceof RuntimeInfoEx) {
                                RuntimeInfoEx rex = (RuntimeInfoEx) runtimeInfo;
                                if (message.startsWith("Buffers")) {
                                    message = message.replace("Buffers:", "").trim();
                                    String value = message.substring(0, message.indexOf(" kB"));
                                    rex.setBuffers(Long.parseLong(value) / MemoryUnit.KB.value());
                                } else if (message.startsWith("Cached")) {
                                    message = message.replace("Cached:", "").trim();
                                    String value = message.substring(0, message.indexOf(" kB"));
                                    rex.setCached(Long.parseLong(value) / MemoryUnit.KB.value());
                                    //break;
                                } else if (message.startsWith("SwapTotal")) {
                                    message = message.replace("SwapTotal:", "").trim();
                                    String value = message.substring(0, message.indexOf(" kB"));
                                    rex.setSwapTotal(Long.parseLong(value) / MemoryUnit.KB.value());
                                    //break;
                                } else if (message.startsWith("SwapFree")) {
                                    message = message.replace("SwapFree:", "").trim();
                                    String value = message.substring(0, message.indexOf(" kB"));
                                    rex.setSwapFree(Long.parseLong(value) / MemoryUnit.KB.value());
                                    //break;
                                }
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
            } finally {
                if (this.isRemote && executor != null) {
                    ((SSHExecutor) executor).close();
                }
            }
            return runtimeInfo;
        }

        @Override
        public double getProcessorUsage() throws IOException {
            double cpu = 0.0;
            try {
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "uptime".split(" "));
                Executor<?> executor = buildExecutor(args);
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
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "cat /proc/meminfo".split(" "));
                Executor<?> executor = buildExecutor(args);
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
                Collections.addAll(args, "cat /proc/meminfo".split(" "));
                Executor<?> executor = buildExecutor(args);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
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
                Collections.addAll(args, "df -k --total".split(" "));
                Executor<?> executor = buildExecutor(args);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("/dev/sd")) {
                            String[] values = message.replace(message.substring(0, message.indexOf(' ')), "").trim().split(" ");
                            mem += Long.parseLong(values[0]) / MemoryUnit.MB.value();
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
                List<String> args = new ArrayList<>();
                Collections.addAll(args, "df -k --total".split(" "));
                Executor<?> executor = buildExecutor(args);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("/dev/sd")) {
                            String[] values = message.replace(message.substring(0, message.indexOf(' ')), "").trim().split(" ");
                            mem += Long.parseLong(values[1]) / MemoryUnit.MB.value();
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
        private final List<String> messages = new ArrayList<>();
        @Override
        public void consume(String message) {
            messages.add(message.replace("\r", ""));
        }
        List<String> getMessages() { return this.messages; }
    }

}
