package ro.cs.tao.utils;

import org.apache.commons.lang.SystemUtils;
import ro.cs.tao.utils.executors.Executor;
import ro.cs.tao.utils.executors.ExecutorType;
import ro.cs.tao.utils.executors.OutputAccumulator;
import ro.cs.tao.utils.executors.ProcessExecutor;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DockerHelper {

    private static final boolean isDockerFound;

    static {
        String systemPath = System.getenv("Path");
        if (systemPath == null) {
            systemPath = System.getenv("PATH");
        }
        boolean canUseDocker = false;
        if (systemPath != null) {
            String[] paths = systemPath.split(File.pathSeparator);
            Path currentPath;
            for (String path : paths) {
                currentPath = Paths.get(path)
                        .resolve(SystemUtils.IS_OS_WINDOWS ? "docker.exe" : "docker");
                if (Files.exists(currentPath)) {
                    canUseDocker = true;
                    break;
                }
            }
        }
        isDockerFound = canUseDocker;
    }
    public static boolean isDockerFound() { return isDockerFound; }

    public static String getDockerVersion() {
        String version = null;
        if (isDockerFound) {
            List<String> arguments = new ArrayList<>();
            arguments.add("docker");
            arguments.add("version");
            try {
                Executor executor = ProcessExecutor.create(ExecutorType.PROCESS,
                                                           InetAddress.getLocalHost().getHostName(),
                                                           arguments);
                OutputAccumulator accumulator = new OutputAccumulator();
                executor.setOutputConsumer(accumulator);
                if (executor.execute(true) == 0) {
                    String output = accumulator.getOutput();
                    String[] lines = output.split("\n");
                    for (String line : lines) {
                        if (line.contains("API version")) {
                            int idx = line.indexOf("API version:") + 19;
                            version = line.substring(idx, line.indexOf(" ", idx + 1)).trim();
                        }
                    }
                }
            } catch (Exception ignored) {

            }
        }
        return version;
    }
}