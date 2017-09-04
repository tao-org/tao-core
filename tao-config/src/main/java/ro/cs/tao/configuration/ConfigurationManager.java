package ro.cs.tao.configuration;

import java.io.IOException;
import java.util.Properties;

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

}
