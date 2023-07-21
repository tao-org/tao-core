package ro.cs.tao.persistence.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ro.cs.tao.configuration.ConfigurationCategory;
import ro.cs.tao.configuration.ConfigurationItem;
import ro.cs.tao.configuration.PersistentConfigurationProvider;
import ro.cs.tao.datasource.param.JavaType;
import ro.cs.tao.persistence.repository.ConfigurationCategoryRepository;
import ro.cs.tao.persistence.repository.ConfigurationRepository;
import ro.cs.tao.utils.StringUtilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("configurationManager")
public class ConfigurationManager
        extends EntityManager<ConfigurationItem, String, ConfigurationRepository>
        implements PersistentConfigurationProvider {

    @Autowired
    private ConfigurationCategoryRepository configurationCategoryRepository;

    @Override
    public List<ConfigurationCategory> getCategories() {
        final List<ConfigurationCategory> results = new ArrayList<>();
        configurationCategoryRepository.findAll().forEach(results::add);
        results.sort(Comparator.comparingInt(ConfigurationCategory::getOrder));
        return results;
    }

    @Override
    public ConfigurationCategory getCategory(int categoryId) {
        return configurationCategoryRepository.findById(categoryId).orElse(null);
    }

    @Override
    public List<ConfigurationItem> getItems() {
        final List<ConfigurationItem> results = new ArrayList<>();
        repository.findAll().forEach(results::add);
        results.sort(Comparator.comparing(ConfigurationItem::getId));
        return results;
    }

    @Override
    public Map<ConfigurationCategory, ConfigurationItem> getGroupedItems() {
        return getItems().stream().collect(Collectors.toMap(ConfigurationItem::getCategory, Function.identity()));
    }

    @Override
    public List<ConfigurationItem> getItems(String filter) {
        return repository.getByFilter(filter);
    }

    @Override
    public List<ConfigurationItem> getCategoryItems(ConfigurationCategory category) {
        return repository.getByCategory(category.getId());
    }

    @Override
    public ConfigurationItem getItem(String key) {
        return repository.findById(key).orElse(null);
    }

    @Override
    public ConfigurationItem saveItem(ConfigurationItem item) {
        return repository.save(item);
    }

    @Override
    protected String identifier() {
        return "key";
    }

    @Override
    protected boolean checkEntity(ConfigurationItem entity) {
        return entity != null && !StringUtilities.isNullOrEmpty(entity.getId()) &&
                !StringUtilities.isNullOrEmpty(entity.getFriendlyName()) &&
                !JavaType.isValid(entity.getType()) && entity.getCategory() != null;
    }
}
