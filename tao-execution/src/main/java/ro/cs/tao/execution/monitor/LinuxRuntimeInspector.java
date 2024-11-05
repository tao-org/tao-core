package ro.cs.tao.execution.monitor;

import ro.cs.tao.component.SystemVariable;
import ro.cs.tao.topology.NodeRole;
import ro.cs.tao.utils.executors.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class LinuxRuntimeInspector implements NodeRuntimeInspector {

    final String node;
    final String user;
    final String password;
    final boolean isRemote;
    final AuthenticationType authenticationType;
    final Logger logger;

    LinuxRuntimeInspector(String host, String user, String password, boolean remote) {
        this(host, AuthenticationType.PASSWORD, user, password, remote);
    }

    LinuxRuntimeInspector(String host, AuthenticationType authenticationType, String user, String authenticationToken, boolean remote) {
        this.node = host;
        this.user = user;
        this.authenticationType = authenticationType;
        this.password = authenticationToken;
        this.isRemote = remote;
        this.logger = Logger.getLogger(getClass().getName());
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
    public void initialize(String host, String user, String password, AuthenticationType authType) throws Exception {
        // NO OP
    }

    @Override
    public boolean isIntendedFor(NodeRole role) {
        return true;
    }

    @Override
    public RuntimeInfo getSnapshot() throws Exception {
        RuntimeInfo runtimeInfo = new RuntimeInfoEx();
        runtimeInfo.setCpuTotal(getProcessorUsage());
        runtimeInfo.setTotalMemory(getTotalMemoryMB());
        runtimeInfo.setAvailableMemory(getAvailableMemoryMB());
        runtimeInfo.setDiskTotal(getTotalDiskGB());
        runtimeInfo.setDiskUsed(getUsedDiskGB());
        runtimeInfo.setDiskUnit(MemoryUnit.GB);
        runtimeInfo.setMemoryUnit(MemoryUnit.MB);
        return runtimeInfo;
    }

    @Override
    public RuntimeInfo getInfo() throws Exception {
        RuntimeInfoEx runtimeInfo = new RuntimeInfoEx();
        Executor<?> executor = null;
        try {
            List<String> args = new ArrayList<>();
            Collections.addAll(args, "uptime && cat /proc/meminfo && df -k --total".split(" "));
            executor = buildExecutor(args);
            final OSRuntimeInfo.Consumer consumer = new OSRuntimeInfo.Consumer();
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
                        //if (runtimeInfo instanceof RuntimeInfoEx) {
                            //RuntimeInfoEx rex = (RuntimeInfoEx) runtimeInfo;
                            if (message.startsWith("Buffers")) {
                                message = message.replace("Buffers:", "").trim();
                                String value = message.substring(0, message.indexOf(" kB"));
                                runtimeInfo.setBuffers(Long.parseLong(value) / MemoryUnit.KB.value());
                            } else if (message.startsWith("Cached")) {
                                message = message.replace("Cached:", "").trim();
                                String value = message.substring(0, message.indexOf(" kB"));
                                runtimeInfo.setCached(Long.parseLong(value) / MemoryUnit.KB.value());
                                //break;
                            } else if (message.startsWith("SwapTotal")) {
                                message = message.replace("SwapTotal:", "").trim();
                                String value = message.substring(0, message.indexOf(" kB"));
                                runtimeInfo.setSwapTotal(Long.parseLong(value) / MemoryUnit.KB.value());
                                //break;
                            } else if (message.startsWith("SwapFree")) {
                                message = message.replace("SwapFree:", "").trim();
                                String value = message.substring(0, message.indexOf(" kB"));
                                runtimeInfo.setSwapFree(Long.parseLong(value) / MemoryUnit.KB.value());
                                //break;
                            }
                        //}
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
            OSRuntimeInfo.Consumer consumer = new OSRuntimeInfo.Consumer();
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
            OSRuntimeInfo.Consumer consumer = new OSRuntimeInfo.Consumer();
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
            OSRuntimeInfo.Consumer consumer = new OSRuntimeInfo.Consumer();
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
            OSRuntimeInfo.Consumer consumer = new OSRuntimeInfo.Consumer();
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
            OSRuntimeInfo.Consumer consumer = new OSRuntimeInfo.Consumer();
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