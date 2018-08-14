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

import ro.cs.tao.utils.Platform;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public abstract class RuntimeInfo {

    final String host;
    final String user;
    final String pass;
    final boolean isRemote;

    RuntimeInfo(String host, String user, String password, boolean remote) {
        this.host = host;
        this.user = user;
        this.pass = password;
        this.isRemote = remote;
    }

    public static RuntimeInfo createInspector(String hostName,
                                              String user, String password) throws Exception {
        String localhost = InetAddress.getLocalHost().getHostName();
        if (localhost.equals(hostName)) {
            Platform currentPlatform = Platform.getCurrentPlatform();
            assert currentPlatform != null;
            switch (currentPlatform.getId()) {
                case win:
                    return new Windows(hostName, user, password, false);
                default:
                    return new Linux(hostName, user, password, false);
            }
        } else {
            return new Linux(hostName, user, password, true);
        }
    }

    public abstract double getProcessorUsage();
    public abstract long getTotalMemoryMB();
    public abstract long getAvailableMemoryMB();
    public abstract long getTotalDiskGB();
    public abstract long getUsedDiskGB();

    private static class Windows extends RuntimeInfo {

        Windows(String host, String user, String password,boolean remote) {
            super(host, user, password, remote);
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
                            mem = Long.parseLong(message.split("=")[1]) / ByteUnit.KILOBYTE.value();
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
                Collections.addAll(args, "wmic os get freephysicalmemory /value".split(" "));
                Executor executor = Executor.create(ExecutorType.PROCESS, this.host, args);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("FreePhysicalMemory")) {
                            // WMIC returns total memory in kilobytes
                            mem = Long.parseLong(message.split("=")[1]) / ByteUnit.KILOBYTE.value();
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
                    mem = mem / ByteUnit.GIGABYTE.value();
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
                    mem = total - mem / ByteUnit.GIGABYTE.value();
                }
            } catch (Exception ex) {
                Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
            }
            return mem;
        }
    }

    private static class Linux extends RuntimeInfo {

        Linux(String host, String user, String password, boolean remote) {
            super(host, user, password, remote);
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
                executor.setPassword(this.pass);
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
                executor.setPassword(this.pass);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("MemTotal")) {
                            message = message.replace("MemTotal:", "").trim();
                            String value = message.substring(0, message.indexOf(" kB"));
                            mem = Long.parseLong(value) / ByteUnit.KILOBYTE.value();
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
                executor.setPassword(this.pass);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("MemFree")) {
                            message = message.replace("MemFree:", "").trim();
                            String value = message.substring(0, message.indexOf(" kB"));
                            mem = Long.parseLong(value) / ByteUnit.KILOBYTE.value();
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
                executor.setPassword(this.pass);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("total")) {
                            String[] values = message.replace("total", "").trim().split(" ");
                            mem = Long.parseLong(values[0]) / ByteUnit.MEGABYTE.value();
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
                executor.setPassword(this.pass);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if (executor.execute(false) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (message.startsWith("total")) {
                            String[] values = message.replace("total", "").trim().split(" ");
                            mem = Long.parseLong(values[1]) / ByteUnit.MEGABYTE.value();
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
        private List<String> messages = new ArrayList<>();
        @Override
        public void consume(String message) {
            messages.add(message);
        }
        List<String> getMessages() { return this.messages; }
    }

}
