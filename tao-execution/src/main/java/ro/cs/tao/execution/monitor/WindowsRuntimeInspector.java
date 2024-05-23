package ro.cs.tao.execution.monitor;

import ro.cs.tao.topology.NodeRole;
import ro.cs.tao.utils.executors.AuthenticationType;
import ro.cs.tao.utils.executors.Executor;
import ro.cs.tao.utils.executors.ExecutorType;
import ro.cs.tao.utils.executors.MemoryUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

class WindowsRuntimeInspector implements NodeRuntimeInspector {

    final String node;
    final String user;
    final String password;
    final boolean isRemote;
    final AuthenticationType authenticationType;
    final Logger logger;

    WindowsRuntimeInspector(String host, AuthenticationType authenticationType, String user, String password, boolean remote) {
        this.node = host;
        this.user = user;
        this.authenticationType = authenticationType;
        this.password = password;
        this.isRemote = remote;
        this.logger = Logger.getLogger(getClass().getName());
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
        RuntimeInfo runtimeInfo = new RuntimeInfo();
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
        RuntimeInfo runtimeInfo = new RuntimeInfo();
        List<String> args = new ArrayList<>();
        Collections.addAll(args, "cmd", "/c");
        args.add("wmic cpu get loadpercentage /value && " +
                         "wmic os get totalvisiblememorysize /value && " +
                         "wmic os get freephysicalmemory /value && " +
                         "wmic logicaldisk get size /value && " +
                         "wmic logicaldisk get freespace /value");
        Executor<?> executor = Executor.create(ExecutorType.PROCESS, this.node, args);
        OSRuntimeInfo.Consumer consumer = new OSRuntimeInfo.Consumer();
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
            OSRuntimeInfo.Consumer consumer = new OSRuntimeInfo.Consumer();
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
            OSRuntimeInfo.Consumer consumer = new OSRuntimeInfo.Consumer();
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
            OSRuntimeInfo.Consumer consumer = new OSRuntimeInfo.Consumer();
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
            OSRuntimeInfo.Consumer consumer = new OSRuntimeInfo.Consumer();
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
            OSRuntimeInfo.Consumer consumer = new OSRuntimeInfo.Consumer();
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
