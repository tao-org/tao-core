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
 * @author Cosmin Cara
 */
public class SSHTest {

    public static void main(String[] args) {
        //executeSingleTest();
        execDockerInfoAsUser();
        execDockerInfoAsSudo();
    }

    private static void executeSingleTest() {
        List<String> args = new ArrayList<String>() {{
            add("cd");
            add("~");
            add(Executor.SHELL_COMMAND_SEPARATOR);
            add("rm");
            add("-fr");
            add("sample_folder");
            add(Executor.SHELL_COMMAND_SEPARATOR);
            add("mkdir");
            add("sample_folder");
            add(Executor.SHELL_COMMAND_SEPARATOR);
            add("chmod");
            add("777");
            add("sample_folder");
            add(Executor.SHELL_COMMAND_SEPARATOR);
            add("ls");
            add("-l");
        }};
        ExecutionUnit job = new ExecutionUnit(ExecutorType.SSH2,
                                              "192.168.61.20", "u1tao", "pass4u1",
                                              args, ExecutionMode.SUPERUSER.value(),
                                              SSHMode.EXEC);
        OutputConsumer consumer = new DebugOutputConsumer();
        Executor.execute(consumer, 10, job);
    }

    private static void execDockerInfoAsUser() {
        List<String> args = new ArrayList<String>() {{
            add("docker");
            add("info");
        }};
        ExecutionUnit job = new ExecutionUnit(ExecutorType.SSH2,
                                              "192.168.61.20", "u1tao", "pass4u1",
                                              args, ExecutionMode.USER.value(),
                                              SSHMode.EXEC);
        OutputConsumer consumer = new DebugOutputConsumer();
        Executor.execute(consumer, 10, job);
    }

    private static void execDockerInfoAsSudo() {
        List<String> args = new ArrayList<String>() {{
            add("docker");
            add("info");
        }};
        ExecutionUnit job = new ExecutionUnit(ExecutorType.SSH2,
                                              "192.168.61.20", "u1tao", "pass4u1",
                                              args, ExecutionMode.SUPERUSER.value(),
                                              SSHMode.EXEC);
        OutputConsumer consumer = new DebugOutputConsumer();
        Executor.execute(consumer, 10, job);
    }
}
