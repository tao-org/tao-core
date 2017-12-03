package ro.cs.tao.configuration;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
public class ConfigurationManager {
    private static final ConfigurationManager instance;

    private final Properties settings;

    static {
        instance = new ConfigurationManager();
    }

    public static ConfigurationManager getInstance() { return instance; }

    private ConfigurationManager() {
        this.settings = new Properties();
        try {
            this.settings.load(ConfigurationManager.class.getResourceAsStream("/tao.properties"));
        } catch (IOException ignored) {
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
}
