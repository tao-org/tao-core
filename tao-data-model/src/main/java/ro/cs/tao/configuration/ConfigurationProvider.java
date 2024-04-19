package ro.cs.tao.configuration;

import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * A configuration provider is responsible for retrieving properties from files.
 * Extenders of the framework may implement this interface and register the implementation at startup.
 *
 * @author Cosmin Cara
 */
public interface ConfigurationProvider {
    String APP_HOME = "app.home";
    /**
     * Returns the path from which TAO was launched
     */
    Path getApplicationHome();
    /**
     * Returns the value of the requested property or null if not found
     */
    String getValue(String name);
    /**
     * Returns the value of the requested property, or the given default value if not found
     */
    String getValue(String name, String defaultValue);
    /**
     * Returns the boolean value of the requested property
     */
    boolean getBooleanValue(String name);
    /**
     * Returns the keys and their values of the properties that contain the given name filter.
     * @param filter The name filter (substring)
     */
    Map<String, String> getValues(String filter);
    /**
     * Returns all the properties
     */
    Map<String, String> getAll();
    /**
     * Sets the property identified by the key to the given value.
     * @param name The key of the property
     * @param value The value
     */
    void setValue(String name, String value);
    /**
     * Adds the collection of properties to the already existing ones, overwriting the ones that exist in both collections
     * @param properties The new properties to be added
     */
    void putAll(Properties properties);
    /**
     * Returns the location of the 'scripts' folder of the running instance
     */
    Path getScriptsFolder();
    /**
     * Sets the location of the 'scripts' folder of the running instance
     */
    void setScriptsFolder(Path folder);
    /**
     * Returns the location of the 'config' folder of the running instance
     */
    Path getConfigurationFolder();
    /**
     * Sets the location of the 'config' folder of the running instance
     */
    void setConfigurationFolder(Path folder);

    default Map<String, String> getSystemEnvironment() {
        return System.getenv();
    }

    default void setSystemEnvironment(Map<String, String> environment) {

    }

    default void setPersistentConfigurationProvider(PersistentConfigurationProvider provider) {

    }
}
