package ro.cs.tao.configuration;

import java.util.List;
import java.util.Map;

/**
 * Provides operations for managing persistent configuration data, including
 * categories and configuration items. This interface defines methods for
 * retrieving, organizing, and saving configuration data.
 */
public interface PersistentConfigurationProvider {

    /* Categories section */
    List<ConfigurationCategory> getCategories();
    ConfigurationCategory getCategory(int categoryId);
    ConfigurationCategory save(ConfigurationCategory category);
    ConfigurationCategory update(ConfigurationCategory category);

    /* Configuration items section */
    List<ConfigurationItem> getItems();
    List<ConfigurationItem> getItems(String filter);
    List<ConfigurationItem> getPerUserItems();
    Map<String, List<ConfigurationItem>> getGroupedItems();
    List<ConfigurationItem> getCategoryItems(ConfigurationCategory category);
    ConfigurationItem getItem(String key);
    ConfigurationItem saveItem(ConfigurationItem item);
}
