package ro.cs.tao.configuration;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.ServiceLoader;

public class ConfigurationManager {
    private static ConfigurationProvider configurationProvider;

    static {
        final ServiceLoader<ConfigurationProvider> loader = ServiceLoader.load(ConfigurationProvider.class);
        final Iterator<ConfigurationProvider> iterator = loader.iterator();
        if (iterator.hasNext()) {
            configurationProvider = iterator.next();
        } else {
            configurationProvider = new EmptyConfigurationProvider();
            final String userHome = System.getProperty("user.home");
            configurationProvider.setValue("products.location", userHome);
            configurationProvider.setScriptsFolder(Paths.get(userHome));
            configurationProvider.setConfigurationFolder(Paths.get(userHome));
        }
    }

    private ConfigurationManager() {
    }

    public static void setConfigurationProvider(ConfigurationProvider provider) {
        configurationProvider = provider;
    }

    public static ConfigurationProvider getInstance() { return configurationProvider; }
}
