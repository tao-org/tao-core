import ro.cs.tao.utils.executors.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class SSHTest {

    public static void main(String[] args) {
        //executeSingleTest();
        //execDockerInfoAsUser();
        execDockerInfoAsSudo();
        //execDockerVersionAsUser();
        System.exit(0);
    }

    private static void execDockerVersionAsUser() {
        List<String> args = new ArrayList<String>() {{
            add("docker");
            add("-v");
            //add("echo $?");
            //add("sudo yum install docker");

        }};
        ExecutionUnit job = new ExecutionUnit(ExecutorType.SSH2,
                                              //        "192.168.61.20", "u1tao", "pass4u1",
                                              "192.168.61.30", "u1", "pass4u1",
                                              args, ExecutionMode.USER.value(),
                                              SSHMode.EXEC);
        OutputConsumer consumer = new DebugOutputConsumer();
        System.out.println("Return code: " + Executor.execute(consumer, 10, job));
        //return String.valueOf(consumer);
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
        List<String> arguments = new ArrayList<String>() {{
            add("docker");
            add("info");
        }};
        //final String certificate = new String(Files.readAllBytes(Paths.get("C:\\SSL\\sen4stat")));
        final String certificate = "C:\\SSL\\sen4stat";
        ExecutionUnit job = new ExecutionUnit(ExecutorType.SSH2,
                "45.130.29.142", "eouser", certificate,
                arguments, ExecutionMode.USER.value(),
                SSHMode.EXEC);
        job.setCertificate(certificate);
        OutputAccumulator consumer = new OutputAccumulator();
        if (Executor.execute(consumer, 10, job) == 0) {
            System.out.println(consumer.getOutput());
        }
    }
}
