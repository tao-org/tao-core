package ro.cs.tao.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Fallback provider if no other ConfigurationProvider is registered.
 * It is not backed by a file, hence the properties added at runtime to it will not be persistent.
 *
 * @author Cosmin Cara
 */
public class EmptyConfigurationProvider implements ConfigurationProvider {
    private final Properties properties;
    private Path scriptFolder;
    private Path configFolder;

    public EmptyConfigurationProvider() {
        this.properties = new Properties();
    }

    @Override
    public Path getApplicationHome() {
        return this.properties.containsKey(APP_HOME) ? Paths.get(this.properties.getProperty(APP_HOME)) : null;
    }

    @Override
    public String getValue(String name) {
        return this.properties.getProperty(name);
    }

    @Override
    public String getValue(String name, String defaultValue) {
        return this.properties.getProperty(name, defaultValue);
    }

    @Override
    public boolean getBooleanValue(String name) {
        return Boolean.parseBoolean(this.properties.getProperty(name, "false"));
    }

    @Override
    public Map<String, String> getValues(String filter) {
        final Map<String, String> entries = new HashMap<>();
        this.properties.stringPropertyNames().forEach(name -> {
            if (name.startsWith(filter)) {
                entries.put(name, this.properties.getProperty(name));
            }
        });
        return entries;
    }

    @Override
    public Map<String, String> getAll() {
        final Map<String, String> entries = new HashMap<>();
        this.properties.stringPropertyNames().forEach(n -> entries.put(n, this.properties.getProperty(n)));
        return entries;
    }

    @Override
    public void setValue(String name, String value) {
        this.properties.put(name, value);
    }

    @Override
    public void putAll(Properties properties) {
        this.properties.putAll(properties);
    }

    @Override
    public Path getScriptsFolder() {
        return this.scriptFolder;
    }

    @Override
    public void setScriptsFolder(Path folder) {
        this.scriptFolder = folder;
    }

    @Override
    public Path getConfigurationFolder() {
        return this.configFolder;
    }

    @Override
    public void setConfigurationFolder(Path folder) {
        this.configFolder = folder;
    }
}
