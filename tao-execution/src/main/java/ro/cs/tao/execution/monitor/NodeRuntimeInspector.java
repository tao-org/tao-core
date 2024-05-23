package ro.cs.tao.execution.monitor;

import ro.cs.tao.topology.NodeRole;
import ro.cs.tao.utils.executors.AuthenticationType;

import java.io.IOException;

public interface NodeRuntimeInspector {

    boolean isIntendedFor(NodeRole role);

    void initialize(String host, String user, String password, AuthenticationType authType) throws Exception;

    double getProcessorUsage() throws IOException;
    long getTotalMemoryMB() throws IOException;
    long getAvailableMemoryMB() throws IOException;
    long getTotalDiskGB() throws IOException;
    long getUsedDiskGB() throws IOException;
    RuntimeInfo getInfo() throws Exception;
    RuntimeInfo getSnapshot() throws Exception;

}
