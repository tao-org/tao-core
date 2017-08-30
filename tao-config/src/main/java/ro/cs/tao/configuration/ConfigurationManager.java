package ro.cs.tao.configuration;

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
        //TODO: load them from somewhere
        this.settings.put("product.location", "E:\\img");
    }

    public String getValue(String name) {
        return this.settings.getProperty(name);
    }

}
