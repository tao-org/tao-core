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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Singleton manager for handling the settings defined in the configuration file.
 * @author Cosmin Cara
 */
public class TaoConfigurationProvider implements ConfigurationProvider {
    public static final String APP_HOME = "app.home";
    private static TaoConfigurationProvider instance;
    private static final String CONFIG_FILE_NAME = "tao.properties";
    // this field may be set by the launcher of the services
    private Path configFolder;
    // this field may be set by the launcher of the services
    private Path scriptsFolder;
    private Properties settings;

    public static TaoConfigurationProvider getInstance() {
        if (instance == null) {
            instance = new TaoConfigurationProvider();
        }
        return instance;
    }

    public TaoConfigurationProvider() {
        settings = new Properties();
        try {
            settings.load(TaoConfigurationProvider.class.getResourceAsStream("/ro/cs/tao/configuration/" + CONFIG_FILE_NAME));
        } catch (IOException ignored) {
        }
        if (instance == null) {
            instance = this;
        }
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
        return this.settings.getProperty(name, null);
    }

    @Override
    public String getValue(String name, String defaultValue) {
        return this.settings.getProperty(name, defaultValue);
    }

    @Override
    public boolean getBooleanValue(String name) {
        String strValue = this.settings.getProperty(name);
        boolean returnValue = false;
        if ("1".equalsIgnoreCase(strValue) || "yes".equalsIgnoreCase(strValue) ||
                "true".equalsIgnoreCase(strValue) || "on".equalsIgnoreCase(strValue)) {
            returnValue = true;
        }
        return returnValue;
    }

    @Override
    public Map<String, String> getValues(String filter) {
        return this.settings.entrySet().stream()
                .filter(e -> ((String) e.getKey()).contains(filter)).collect(
                        Collectors.toMap(
                                e -> (String) e.getKey(),
                                e -> (String) e.getValue()
                        ));
    }

    @Override
    public Map<String, String> getAll() {
        return this.settings.entrySet().stream().collect(
                Collectors.toMap(
                        e -> (String) e.getKey(),
                        e -> (String) e.getValue()
                ));
    }

    @Override
    public void setValue(String name, String value) { this.settings.setProperty(name, value); }

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
}
