package ro.cs.tao.configuration;

import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

public interface ConfigurationProvider {
    String APP_HOME = "app.home";
    Path getApplicationHome();
    String getValue(String name);
    String getValue(String name, String defaultValue);
    boolean getBooleanValue(String name);
    Map<String, String> getValues(String filter);
    Map<String, String> getAll();
    void setValue(String name, String value);
    void putAll(Properties properties);
    Path getScriptsFolder();
    void setScriptsFolder(Path folder);
    Path getConfigurationFolder();
    void setConfigurationFolder(Path folder);
}
