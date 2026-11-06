package ro.cs.tao.execution.monitor;

import com.sun.management.OperatingSystemMXBean;
import ro.cs.tao.component.SystemVariable;
import ro.cs.tao.topology.NodeRole;
import ro.cs.tao.utils.executors.AuthenticationType;
import ro.cs.tao.utils.executors.MemoryUnit;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

/**
 * The WindowsRuntimeInspector class provides methods to collect runtime information
 * about a Windows operating system node. It gathers details such as CPU usage,
 * memory, and disk statistics by interacting with the Windows Management Instrumentation
 * Command-line (WMIC). This class implements the NodeRuntimeInspector interface.
 *
 * The collected information includes system metrics such as:
 * - CPU usage percentage
 * - Total and available memory in MB
 * - Total and used disk space in GB
 *
 * Additionally, this class can execute commands remotely or locally depending on the
 * specified configuration.
 */
class WindowsRuntimeInspector implements NodeRuntimeInspector {

    final String node;
    final String user;
    final String password;
    final boolean isRemote;
    final AuthenticationType authenticationType;
    final Logger logger;
    final File root;
    final OperatingSystemMXBean mxBean;

    WindowsRuntimeInspector(String host, AuthenticationType authenticationType, String user, String password, boolean remote) {
        this.node = host;
        this.user = user;
        this.authenticationType = authenticationType;
        this.password = password;
        this.isRemote = remote;
        this.root = new File(SystemVariable.ROOT.value());
        this.mxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
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
        return getSnapshot();
    }

    @Override
    public double getProcessorUsage() throws IOException {
        return this.mxBean.getCpuLoad();
    }

    @Override
    public long getTotalMemoryMB() throws IOException {
        return this.mxBean.getTotalMemorySize() / MemoryUnit.MB.value();
    }

    @Override
    public long getAvailableMemoryMB() throws IOException {
        return this.mxBean.getFreeMemorySize() / MemoryUnit.MB.value();
    }

    @Override
    public long getTotalDiskGB() throws IOException {
        return this.root.getTotalSpace() / MemoryUnit.GB.value();
    }

    @Override
    public long getUsedDiskGB() throws IOException {
        return (this.root.getTotalSpace() - this.root.getUsableSpace()) / MemoryUnit.GB.value();
    }
}
