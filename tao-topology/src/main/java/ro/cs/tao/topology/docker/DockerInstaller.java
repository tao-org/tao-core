package ro.cs.tao.topology.docker;

import ro.cs.tao.topology.ITopologyToolInstaller;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.TopologyException;
import ro.cs.tao.topology.executors.DebugOutputConsumer;
import ro.cs.tao.topology.executors.ExecutionMode;
import ro.cs.tao.topology.executors.ExecutionUnit;
import ro.cs.tao.topology.executors.Executor;
import ro.cs.tao.topology.executors.ExecutorType;
import ro.cs.tao.topology.executors.OutputConsumer;
import ro.cs.tao.topology.executors.SSHMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cosmin on 7/17/2017.
 */
public class DockerInstaller implements ITopologyToolInstaller {
    private static final List<String> checkDockerArgs;
    private static final List<String> installDockerArgs;

    static {
        checkDockerArgs = new ArrayList<String>() {{
            add("docker");
            add("-v");
        }};
        installDockerArgs  = new ArrayList<String>() {{
            add("sudo");
            add("yum");
            add("install");
            add("docker");
        }};
    }

    @Override
    public void doInstall(NodeDescription info) {
        if (!isInstalled(info)) {
            ExecutionUnit job = new ExecutionUnit(ExecutorType.SSH2,
                                                  info.getIpAddr(), info.getUserName(), info.getUserPass(),
                                                  installDockerArgs, ExecutionMode.SUPERUSER.value(),
                                                  SSHMode.EXEC);
            OutputConsumer consumer = new DebugOutputConsumer();
            int retCode = Executor.execute(consumer, 10, job);
            if (retCode != DockerCodes.OK) {
                throw new TopologyException("Docker installation failed") {{
                    addAdditionalInfo("Node", info);
                    addAdditionalInfo("Code", retCode);
                }};
            }
        }
    }

    @Override
    public void doUninstall(NodeDescription info) {

    }

    private boolean isInstalled(NodeDescription info) {
        ExecutionUnit job = new ExecutionUnit(ExecutorType.SSH2,
                                              info.getIpAddr(), info.getUserName(), info.getUserPass(),
                                              checkDockerArgs, ExecutionMode.USER.value(),
                                              SSHMode.EXEC);
        OutputConsumer consumer = new DebugOutputConsumer();
        return Executor.execute(consumer, 10, job) == DockerCodes.OK;
    }
}
