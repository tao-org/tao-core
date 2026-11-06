/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Singleton manager for handling the settings defined in the configuration file.
 * @author Cosmin Cara
 */
public class TaoConfigurationProvider implements ConfigurationProvider {
    private static TaoConfigurationProvider instance;
    private static final String CONFIG_FILE_NAME = "tao.properties";
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9.-_]+)\\}");
    // this field may be set by the launcher of the services
    private Path configFolder;
    // this field may be set by the launcher of the services
    private Path scriptsFolder;
    private Properties settings;
    private Map<String, String> environment;
    private PersistentConfigurationProvider dbConfigProvider;
    private UserPersistentConfigurationProvider userConfigProvider;
    private Map<String, List<ConfigurationItemChangeListener>> listeners;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static TaoConfigurationProvider getInstance() {
        if (instance == null) {
            instance = new TaoConfigurationProvider();
        }
        return instance;
    }

    public TaoConfigurationProvider() {
        settings = new Properties();
        try {
            Path path = Paths.get(TaoConfigurationProvider.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            path = path.getParent().getParent().resolve("config").resolve(CONFIG_FILE_NAME);
            try (InputStream is = Files.newInputStream(path)) {
                settings.load(is);
            }
        } catch (Exception e) {
            try {
                settings.load(TaoConfigurationProvider.class.getResourceAsStream("/ro/cs/tao/configuration/" + CONFIG_FILE_NAME));
            } catch (IOException ignored) {
                throw new RuntimeException(e);
            }
        }
        if (instance == null) {
            instance = this;
        }
    }

    @Override
    public void setPersistentConfigurationProvider(PersistentConfigurationProvider provider) {
        this.dbConfigProvider = provider;
    }

    @Override
    public void setUserPersistentConfigurationProvider(UserPersistentConfigurationProvider provider) {
        this.userConfigProvider = provider;
    }

    @Override
    public Path getScriptsFolder() { return scriptsFolder; }

    @Override
    public void setScriptsFolder(Path folder) { scriptsFolder = folder; }

    @Override
    public void putAll(Properties properties) {
        if (settings != null) {
            settings.putAll(properties);
        } else {
            settings = properties;
        }
    }

    @Override
    public Path getConfigurationFolder() { return configFolder; }

    @Override
    public void setConfigurationFolder(Path folder) { configFolder = folder; }

    @Override
    public Path getApplicationHome() {
        final String value = this.settings.getProperty(APP_HOME);
        return value != null ? Paths.get(value) : null;
    }

    @Override
    public String getValue(String name) {
        return getValue(name, null);
    }

    @Override
    public String getValue(String name, String defaultValue) {
        String value = getDbValue(name);
        if (value == null) {
            value = this.settings.getProperty(name, defaultValue);
        }
        if (value != null) {
            final Matcher matcher = PROPERTY_PATTERN.matcher(value);
            while (matcher.find()) {
                final String otherProperty = matcher.group(1);
                value = value.replaceAll("\\$\\{" + otherProperty + "\\}", getValue(otherProperty));
            }
        }
        return value;
    }

    @Override
    public boolean getBooleanValue(String name) {
        String strValue = getDbValue(name);
        if (strValue == null) {
            strValue = this.settings.getProperty(name);
        }
        return "1".equalsIgnoreCase(strValue) || "yes".equalsIgnoreCase(strValue) ||
                "true".equalsIgnoreCase(strValue) || "on".equalsIgnoreCase(strValue);
    }

    @Override
    public int getIntValue(String name) {
        String strValue = getDbValue(name);
        if (strValue == null) {
            strValue = this.settings.getProperty(name);
        }
        return Integer.parseInt(strValue);
    }

    @Override
    public Map<String, String> getValues(String filter) {
        final Map<String, String> results = new HashMap<>();
        if (this.dbConfigProvider != null) {
            final List<ConfigurationItem> items = this.dbConfigProvider.getItems(filter);
            if (items != null) {
                for (ConfigurationItem item : items) {
                    results.put(item.getId(), item.getValue());
                }
            }
        }
        results.putAll(this.settings.entrySet().stream()
                                    .filter(e -> ((String) e.getKey()).contains(filter)).collect(
                                            Collectors.toMap(
                                                    e -> (String) e.getKey(),
                                                    e -> (String) e.getValue())));
        return results;
    }

    @Override
    public Map<String, String> getAll() {
        final Map<String, String> results = new HashMap<>();
        if (this.dbConfigProvider != null) {
            results.putAll(this.dbConfigProvider.getItems().stream().collect(Collectors.toMap(ConfigurationItem::getId, ConfigurationItem::getValue)));
        }
        results.putAll(this.settings.entrySet().stream().collect(Collectors.toMap(e -> (String) e.getKey(),
                                                                                  e -> (String) e.getValue())));
        return results;
    }

    @Override
    public void setValue(String name, String value) {
        this.settings.setProperty(name, value);
        if (this.dbConfigProvider != null) {
            ConfigurationItem item = this.dbConfigProvider.getItem(name);
            if (item != null) {
                item.setValue(value);
                item.setLastUpdated(LocalDateTime.now());
                this.dbConfigProvider.saveItem(item);
                notifyListeners(item);
            }
        }
    }

    @Override
    public String getUserValue(String userId, String name) {
        return getUserValue(userId, name, null);
    }

    @Override
    public String getUserValue(String userId, String name, String defaultValue) {
        String value = getDbValue(userId, name);
        if (value == null) {
            value = this.settings.getProperty(name, defaultValue);
        }
        if (value != null) {
            final Matcher matcher = PROPERTY_PATTERN.matcher(value);
            while (matcher.find()) {
                final String otherProperty = matcher.group(1);
                value = value.replaceAll("\\$\\{" + otherProperty + "\\}", getValue(otherProperty));
            }
        }
        return value;
    }

    @Override
    public boolean getUserBooleanValue(String userId, String name) {
        String strValue = getDbValue(userId, name);
        if (strValue == null) {
            strValue = getDbValue(name);
        }
        if (strValue == null) {
            strValue = this.settings.getProperty(name);
        }
        return "1".equalsIgnoreCase(strValue) || "yes".equalsIgnoreCase(strValue) ||
               "true".equalsIgnoreCase(strValue) || "on".equalsIgnoreCase(strValue);
    }

    @Override
    public int getUserIntValue(String userId, String name) {
        String strValue = getDbValue(userId, name);
        if (strValue == null) {
            strValue = getDbValue(name);
        }
        if (strValue == null) {
            strValue = this.settings.getProperty(name);
        }
        return Integer.parseInt(strValue);
    }

    @Override
    public void setUserValue(String userId, String name, String value) {
        if (this.userConfigProvider != null) {
            UserConfigurationItem item = this.userConfigProvider.getItem(userId, name);
            if (item == null && this.dbConfigProvider != null) {
                final ConfigurationItem configurationItem = this.dbConfigProvider.getItem(name);
                if (configurationItem != null) {
                    item = new UserConfigurationItem(userId, configurationItem);
                }
            }
            if (item == null) {
                item = new UserConfigurationItem();
                item.setUserId(userId);
                item.setKey(name);
            }
            item.setValue(value);
            item.setLastUpdated(LocalDateTime.now());
            this.userConfigProvider.saveItem(item);
            notifyListeners(item);
        }
    }

    @Override
    public Map<String, String> getSystemEnvironment() {
        return this.environment != null ? this.environment : System.getenv();
    }

    @Override
    public void setSystemEnvironment(Map<String, String> environment) {
        this.environment = new HashMap<>(environment);
    }

    @Override
    public synchronized void addConfigurationItemChangeListener(String[] itemKeys, ConfigurationItemChangeListener listener) {
        if (itemKeys == null || itemKeys.length == 0) {
            return;
        }
        if (this.listeners == null) {
            this.listeners = new HashMap<>();
        }
        for (String itemKey : itemKeys) {
            if (!this.listeners.containsKey(itemKey)) {
                this.listeners.put(itemKey, new ArrayList<>());
            }
            this.listeners.get(itemKey).add(listener);
        }
    }

    private String getDbValue(String name) {
        String value = null;
        if (this.dbConfigProvider != null) {
            final ConfigurationItem item = this.dbConfigProvider.getItem(name);
            if (item != null) {
                value = item.getValue();
                if (value != null) {
                    final Matcher matcher = PROPERTY_PATTERN.matcher(value);
                    while (matcher.find()) {
                        final String otherProperty = matcher.group(1);
                        value = value.replaceAll("\\$\\{" + otherProperty + "\\}", getValue(otherProperty));
                    }
                }
            }
        }
        return value;
    }

    private String getDbValue(String userId, String name) {
        String value = null;
        if (this.userConfigProvider != null) {
            final UserConfigurationItem item = this.userConfigProvider.getItem(userId, name);
            if (item != null) {
                value = item.getValue();
                if (value != null) {
                    final Matcher matcher = PROPERTY_PATTERN.matcher(value);
                    while (matcher.find()) {
                        final String otherProperty = matcher.group(1);
                        value = value.replaceAll("\\$\\{" + otherProperty + "\\}", getValue(otherProperty));
                    }
                }
            }
        }
        return value;
    }

    private void externalizeProperties(Path target) throws IOException {
        byte[] buffer = new byte[1024];
        try (BufferedInputStream is = new BufferedInputStream(TaoConfigurationProvider.class.getResourceAsStream("/ro/cs/tao/configuration/" + CONFIG_FILE_NAME));
             OutputStream os = new BufferedOutputStream(Files.newOutputStream(target))) {
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
        }
    }

    private void notifyListeners(ConfigurationItem item) {
        if (this.listeners != null) {
            List<ConfigurationItemChangeListener> list = this.listeners.get(item.getId());
            if (list != null) {
                for (ConfigurationItemChangeListener listener : list) {
                    executor.execute(() -> {
                        listener.onChanged(item);
                    });
                }
            }
        }
    }

    private void notifyListeners(UserConfigurationItem item) {
        if (this.listeners != null) {
            List<ConfigurationItemChangeListener> list = this.listeners.get(item.getKey());
            if (list != null) {
                for (ConfigurationItemChangeListener listener : list) {
                    executor.execute(() -> {
                        listener.onChanged(item);
                    });
                }
            }
        }
    }
}
