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
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Singleton manager for handling the settings defined in the configuration file.
 * @author Cosmin Cara
 */
public class ConfigurationManager {
    private static ConfigurationManager instance;
    private static final String CONFIG_FILE_NAME = "tao.properties";
    // this field may be set by the launcher of the services
    private static Path configFolder;
    // this field may be set by the launcher of the services
    private static Path scriptsFolder;
    private Properties settings;

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    private ConfigurationManager() {
        if (settings == null) {
            settings = new Properties();
            try {
                settings.load(ConfigurationManager.class.getResourceAsStream("/ro/cs/tao/configuration/" + CONFIG_FILE_NAME));
            } catch (IOException ignored) {
            }
        }
    }

    public Path getScriptsFolder() { return scriptsFolder; }

    public String getValue(String name) {
        return this.settings.getProperty(name, null);
    }

    public String getValue(String name, String defaultValue) {
        return this.settings.getProperty(name, defaultValue);
    }

    public boolean getBooleanValue(String name) {
        String strValue = this.settings.getProperty(name);
        boolean returnValue = false;
        if ("1".equalsIgnoreCase(strValue) || "yes".equalsIgnoreCase(strValue) ||
                "true".equalsIgnoreCase(strValue) || "on".equalsIgnoreCase(strValue)) {
            returnValue = true;
        }
        return returnValue;
    }

    public Map<String, String> getValues(String filter) {
        return this.settings.entrySet().stream()
                .filter(e -> ((String) e.getKey()).contains(filter)).collect(
                        Collectors.toMap(
                                e -> (String) e.getKey(),
                                e -> (String) e.getValue()
                        ));
    }

    public Map<String, String> getAll() {
        return this.settings.entrySet().stream().collect(
                Collectors.toMap(
                        e -> (String) e.getKey(),
                        e -> (String) e.getValue()
                ));
    }

    public void setValue(String name, String value) { this.settings.setProperty(name, value); }

    private void externalizeProperties(Path target) throws IOException {
        byte[] buffer = new byte[1024];
        try (BufferedInputStream is = new BufferedInputStream(ConfigurationManager.class.getResourceAsStream("/ro/cs/tao/configuration/" + CONFIG_FILE_NAME));
                OutputStream os = new BufferedOutputStream(Files.newOutputStream(target))) {
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
        }
    }
}
