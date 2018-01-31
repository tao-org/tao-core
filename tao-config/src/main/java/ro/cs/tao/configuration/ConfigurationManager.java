package ro.cs.tao.configuration;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
public class ConfigurationManager {
    private static final ConfigurationManager instance;
    private static final String CONFIG_FILE_NAME = "tao.properties";
    // this field may be set by the launcher of the services
    private static Path configFolder;
    private Properties settings;

    static {
        instance = new ConfigurationManager();
    }

    public static ConfigurationManager getInstance() { return instance; }

    private ConfigurationManager() {
        if (settings == null) {
            settings = new Properties();
            try {
                if (configFolder != null) {
                    Path configFile = configFolder.resolve(CONFIG_FILE_NAME);
                    if (!Files.exists(configFile)) {
                        externalizeProperties(configFile);
                    }
                    settings.load(Files.newInputStream(configFile));
                } else {
                    settings.load(ConfigurationManager.class.getResourceAsStream("/" + CONFIG_FILE_NAME));
                }
            } catch (IOException ignored) {
            }
        }
    }

    public String getValue(String name) {
        return this.settings.getProperty(name);
    }

    public Map<String, String> getValues(String filter) {
        return this.settings.entrySet().stream()
                .filter(e -> ((String) e.getKey()).contains(filter)).collect(
                        Collectors.toMap(
                                e -> (String) e.getKey(),
                                e -> (String) e.getValue()
                        ));
    }

    public Map<String, String> getAll() {
        return this.settings.entrySet().stream().collect(
                Collectors.toMap(
                        e -> (String) e.getKey(),
                        e -> (String) e.getValue()
                ));
    }

    private void externalizeProperties(Path target) throws IOException {
        byte[] buffer = new byte[1024];
        try (BufferedInputStream is = new BufferedInputStream(ConfigurationManager.class.getResourceAsStream("/tao.properties"))) {
            int read;
            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(target))) {
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
        }
    }
}
