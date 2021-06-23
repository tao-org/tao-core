package ro.cs.tao.utils.executors.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContainerCmdBuilder {

    public static List<String> buildCommandLineArguments(ContainerUnit unit) {
        final List<String> args = new ArrayList<>();
        switch (unit.getType()) {
            case KUBERNETES:
                // TODO
                break;
            case DOCKER:
                args.add("docker");
                args.add("run");
                args.addAll(unit.getArguments());
                final Map<String, String> variables = unit.getEnvironmentVariables();
                if (variables != null) {
                    for (Map.Entry<String, String> entry : variables.entrySet()) {
                        args.add("-e");
                        if (entry.getValue() != null) {
                            args.add(entry.getKey() + "=" + entry.getValue());
                        } else {
                            args.add(entry.getKey());
                        }
                    }
                }
                final Map<String, String> volumeMap = unit.getVolumeMap();
                if (volumeMap != null) {
                    for (Map.Entry<String, String> entry : volumeMap.entrySet()) {
                        args.add("-v");
                        args.add(entry.getKey() + ":" + entry.getValue());
                    }
                }
                final String registry = unit.getContainerRegistry();
                if (registry != null) {
                    args.add(registry + "/" + unit.getContainerName());
                } else {
                    args.add(unit.getContainerName());
                }
            default:
                break;
        }
        return args;
    }
}
