package ro.cs.tao.services.interfaces;

import ro.cs.tao.configuration.ConfigurationManager;

public interface TAOService {
    default boolean isDevModeEnabled() {
        return Boolean.parseBoolean(ConfigurationManager.getInstance().getValue("tao.dev.mode", "false"));
    }
}
