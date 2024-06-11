package ro.cs.tao.utils;

import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.utils.executors.Executor;
import ro.cs.tao.utils.executors.ExecutorType;
import ro.cs.tao.utils.executors.OutputAccumulator;
import ro.cs.tao.utils.executors.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
        Logger.getLogger(DockerHelper.class.getName()).fine("Docker was " + (!isDockerFound ? "not " : "") + "found on system path");
    }
    public static boolean isDockerFound() { return isDockerFound; }

    public static String getDockerVersion() {
        String version = null;
        if (isDockerFound) {
            List<String> arguments = new ArrayList<>();
            arguments.add("docker");
            arguments.add("version");
            try {
                Executor<?> executor = ProcessExecutor.create(ExecutorType.PROCESS,
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

    public static void saveImage(String imageName, Path file) throws IOException {
        if (isDockerFound) {
            List<String> arguments = new ArrayList<>();
            arguments.add("docker");
            arguments.add("save");
            arguments.add("-o");
            arguments.add(FileUtilities.asUnixPath(file, true));
            arguments.add(imageName);
            try {
                Executor<?> executor = ProcessExecutor.create(ExecutorType.PROCESS,
                                                              InetAddress.getLocalHost().getHostName(),
                                                              arguments);
                OutputAccumulator accumulator = new OutputAccumulator();
                executor.setOutputConsumer(accumulator);
                if (executor.execute(true) != 0) {
                    throw new IOException(accumulator.getOutput());
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static void loadImage(Path imageFile) throws IOException {
        if (isDockerFound) {
            List<String> arguments = new ArrayList<>();
            arguments.add("docker");
            arguments.add("load");
            arguments.add("-i");
            arguments.add(FileUtilities.asUnixPath(imageFile, true));
            try {
                Executor<?> executor = ProcessExecutor.create(ExecutorType.PROCESS,
                                                              InetAddress.getLocalHost().getHostName(),
                                                              arguments);
                OutputAccumulator accumulator = new OutputAccumulator();
                executor.setOutputConsumer(accumulator);
                if (executor.execute(true) != 0) {
                    throw new IOException(accumulator.getOutput());
                }
            } catch (Exception ignored) {
            }
        }
    }
}
