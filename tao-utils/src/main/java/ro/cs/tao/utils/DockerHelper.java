package ro.cs.tao.utils;

import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
}
