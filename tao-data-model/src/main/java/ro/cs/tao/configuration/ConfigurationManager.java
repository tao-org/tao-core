package ro.cs.tao.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Singleton that handles the configuration provider registration.
 * If no such provider is registered, an in-memory provider (i.e. that is not backed by a file) will be used.
 *
 * @author Cosmin Cara
 */
public class ConfigurationManager {
    private static ConfigurationProvider configurationProvider;

    static {
        final ServiceLoader<ConfigurationProvider> loader = ServiceLoader.load(ConfigurationProvider.class);
        final Iterator<ConfigurationProvider> iterator = loader.iterator();
        if (iterator.hasNext()) {
            try {
                configurationProvider = iterator.next();
            } catch (Throwable ignored) {
            }
        }
        if (configurationProvider == null) {
            configurationProvider = new EmptyConfigurationProvider();
            final String userHome = System.getProperty("user.home");
            configurationProvider.setValue(ConfigurationProvider.APP_HOME, userHome);
            configurationProvider.setValue("products.location", userHome);
            Path path = Paths.get(userHome);
            configurationProvider.setScriptsFolder(path);
            configurationProvider.setConfigurationFolder(path);
        }
    }

    private ConfigurationManager() {
    }

    public static void setConfigurationProvider(ConfigurationProvider provider) {
        configurationProvider = provider;
    }

    public static ConfigurationProvider getInstance() { return configurationProvider; }
}
