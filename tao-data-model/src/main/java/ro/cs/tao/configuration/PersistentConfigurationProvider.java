package ro.cs.tao.configuration;

import java.util.List;
import java.util.Map;

public interface PersistentConfigurationProvider {

    List<ConfigurationCategory> getCategories();
    ConfigurationCategory getCategory(int categoryId);
    List<ConfigurationItem> getItems();
    List<ConfigurationItem> getItems(String filter);
    Map<String, List<ConfigurationItem>> getGroupedItems();
    List<ConfigurationItem> getCategoryItems(ConfigurationCategory category);
    ConfigurationItem getItem(String key);
    ConfigurationItem saveItem(ConfigurationItem item);
}
