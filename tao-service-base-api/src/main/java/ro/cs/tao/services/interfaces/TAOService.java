package ro.cs.tao.services.interfaces;

import ro.cs.tao.configuration.ConfigurationManager;

/**
 * Base (marker) interface for all TAO service interfaces
 */
public interface TAOService {
    default boolean isDevModeEnabled() {
        return Boolean.parseBoolean(ConfigurationManager.getInstance().getValue("tao.dev.mode", "false"));
    }
}
