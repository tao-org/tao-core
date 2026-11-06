package ro.cs.tao.configuration;

public interface ConfigurationItemChangeListener {
    void onChanged(ConfigurationItem configurationItem);
    default void onChanged(UserConfigurationItem configurationItem) {
    }
}
