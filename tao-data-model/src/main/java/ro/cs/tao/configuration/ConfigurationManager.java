package ro.cs.tao.configuration;

import java.util.Iterator;
import java.util.ServiceLoader;

public class ConfigurationManager {
    private static ConfigurationProvider configurationProvider;

    static {
        final ServiceLoader<ConfigurationProvider> loader = ServiceLoader.load(ConfigurationProvider.class);
        final Iterator<ConfigurationProvider> iterator = loader.iterator();
        if (iterator.hasNext()) {
            configurationProvider = iterator.next();
        }
    }

    private ConfigurationManager() {
    }

    public static void setConfigurationProvider(ConfigurationProvider provider) {
        configurationProvider = provider;
    }

    public static ConfigurationProvider getInstance() { return configurationProvider; }
}
