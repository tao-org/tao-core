package ro.cs.tao.configuration;

import java.util.List;

/**
 * Provides operations for managing persistent user configuration data.
 */
public interface UserPersistentConfigurationProvider {

    List<UserConfigurationItem> getItems(String userId);
    List<UserConfigurationItem> getItems(String userId, String filter);
    UserConfigurationItem getItem(String userId, String key);
    UserConfigurationItem saveItem(UserConfigurationItem item);
    List<UserConfigurationItem> saveItems(List<UserConfigurationItem> items);
}
