package ro.cs.tao.utils.executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ro.cs.tao.utils.ExceptionUtils;
import ro.cs.tao.utils.executors.container.ContainerUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

public class JsonExecutor extends Executor<Object> {
    private final ExecutionUnit unit;

    JsonExecutor(ExecutionUnit unit) {
        super(unit.getHost(), unit.getArguments(), unit.asSuperUser());
        this.unit = unit;
    }

    @Override
    public boolean canConnect() {
        return true;
    }

    @Override
    public int execute(boolean logMessages) throws Exception {
        try {
            final String json = new ObjectMapper().writer().forType(JsonUnitView.class).writeValueAsString(asJsonView(this.unit));
            this.outputConsumer.consume(json);
            if (unit.getJsonPath() != null) {
                try {
                    final Path path = Paths.get(unit.getJsonPath());
                    if (!Files.exists(path)) {
                        Files.write(path, ("{" + json + "}").getBytes(), StandardOpenOption.CREATE);
                    } else {
                        String previous = new String(Files.readAllBytes(path));
                        Files.write(path, (previous.substring(0, previous.length() - 1) + "," + json + "}").getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                    }
                } catch (IOException e) {
                    logger.severe(ExceptionUtils.getStackTrace(logger, e));
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return 0;
    }

    private JsonUnitView asJsonView(ExecutionUnit unit) {
        final JsonUnitView view = new JsonUnitView();
        final Map<String, Object> metadata = unit.getMetadata();
        final ContainerUnit container = unit.getContainerUnit();
        if (container != null) {
            view.setType(container.getType().value());
            view.setImage(container.getContainerName());
            view.setContainerArguments(container.getArguments());
            view.setRegistry(container.getContainerRegistry());
            view.setEnvironment(container.getEnvironmentVariables());
            view.setMounts(container.getVolumeMap());
        }
        view.setCommands(unit.getArguments());
        if (metadata != null ) {
            if (metadata.containsKey("id")) {
                view.setId((long) metadata.get("id"));
            }
            if (metadata.containsKey("name")) {
                view.setName((String) metadata.get("name"));
            }
            if (metadata.containsKey("dependsOn")) {
                view.setDependencies((List<Long>) metadata.get("dependsOn"));
            }
            if (metadata.containsKey("canDelete")) {
                view.setTemporaries((List<String>) metadata.get("canDelete"));
            }
        }
        return view;
    }

    private static class JsonUnitView {
        private long id;
        private String name;
        private String type;
        private String image;
        private String registry;
        private List<String> containerArguments;
        private List<String> commands;
        private Map<String, String> environment;
        private Map<String, String> mounts;
        private List<String> temporaries;
        private List<Long> dependencies;

        JsonUnitView() {
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getRegistry() {
            return registry;
        }

        public void setRegistry(String registry) {
            this.registry = registry;
        }

        public List<String> getContainerArguments() {
            return containerArguments;
        }

        public void setContainerArguments(List<String> containerArguments) {
            this.containerArguments = containerArguments;
        }

        public List<String> getCommands() {
            return commands;
        }

        public void setCommands(List<String> commands) {
            this.commands = commands;
        }

        public Map<String, String> getEnvironment() {
            return environment;
        }

        public void setEnvironment(Map<String, String> environment) {
            this.environment = environment;
        }

        public Map<String, String> getMounts() {
            return mounts;
        }

        public void setMounts(Map<String, String> mounts) {
            this.mounts = mounts;
        }

        public List<String> getTemporaries() {
            return temporaries;
        }

        public void setTemporaries(List<String> temporaries) {
            this.temporaries = temporaries;
        }

        public List<Long> getDependencies() {
            return dependencies;
        }

        public void setDependencies(List<Long> dependencies) {
            this.dependencies = dependencies;
        }
    }
}
