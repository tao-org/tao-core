package ro.cs.tao.execution.monitor;

import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.topology.NodeRole;
import ro.cs.tao.utils.executors.AuthenticationType;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;

public class DefaultRuntimeInspector implements NodeRuntimeInspector {
    String node;
    String user;
    String password;
    boolean isRemote;
    NodeRuntimeInspector inspector;
    AuthenticationType authenticationType;
    final Logger logger;

    public DefaultRuntimeInspector() {
        this.logger = Logger.getLogger(DefaultRuntimeInspector.class.getName());
    }

    @Override
    public boolean isIntendedFor(NodeRole role) {
        return role != NodeRole.K8S;
    }

    @Override
    public void initialize(String host, String user, String password, AuthenticationType authType) throws Exception {
        this.node = host;
        this.user = user;
        this.password = password;
        this.authenticationType = authType;
        String localhost = InetAddress.getLocalHost().getHostName();
        if (localhost.equals(host)) {
            this.inspector = SystemUtils.IS_OS_WINDOWS
                   ? new WindowsRuntimeInspector(host, authType, user, password, false)
                   : new LinuxRuntimeInspector(host, authType, user, password, false);
        } else {
            this.inspector = new LinuxRuntimeInspector(host, authType, user, password, true);
        }
    }

    @Override
    public double getProcessorUsage() throws IOException {
        if (this.inspector == null) {
            throw new RuntimeException("Not initialized");
        }
        return this.inspector.getProcessorUsage();
    }

    @Override
    public long getTotalMemoryMB() throws IOException {
        if (this.inspector == null) {
            throw new RuntimeException("Not initialized");
        }
        return this.inspector.getTotalMemoryMB();
    }

    @Override
    public long getAvailableMemoryMB() throws IOException {
        if (this.inspector == null) {
            throw new RuntimeException("Not initialized");
        }
        return this.inspector.getAvailableMemoryMB();
    }

    @Override
    public long getTotalDiskGB() throws IOException {
        if (this.inspector == null) {
            throw new RuntimeException("Not initialized");
        }
        return this.inspector.getTotalDiskGB();
    }

    @Override
    public long getUsedDiskGB() throws IOException {
        if (this.inspector == null) {
            throw new RuntimeException("Not initialized");
        }
        return this.inspector.getUsedDiskGB();
    }

    @Override
    public RuntimeInfo getInfo() throws Exception {
        if (this.inspector == null) {
            throw new RuntimeException("Not initialized");
        }
        return this.inspector.getInfo();
    }

    @Override
    public RuntimeInfo getSnapshot() throws Exception {
        if (this.inspector == null) {
            throw new RuntimeException("Not initialized");
        }
        return this.inspector.getSnapshot();
    }
}
