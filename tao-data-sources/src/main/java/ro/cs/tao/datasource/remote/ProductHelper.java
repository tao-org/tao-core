/*
 * Copyright (C) 2017 CS ROMANIA
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
package ro.cs.tao.datasource.remote;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cosmin Cara
 */
public abstract class ProductHelper {
    protected String name;
    protected String id;
    protected String sensingDate;
    protected String version;

    public ProductHelper() {}

    public ProductHelper(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!verifyProductName(name)) {
            throw new IllegalArgumentException(String.format("The product name %s doesn't match the expected pattern", name));
        }
        this.name = name;
    }

    public Class<? extends ProductHelper> getHelperClass() { return getClass(); }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() { return version; }

    public void setVersion(String version) { this.version = version; }

    public String getSensingDate() { return sensingDate; }

    public void setSensingDate(String date) { this.sensingDate = date; }

    public abstract String getMetadataFileName();

    public abstract String getProductRelativePath();

    public abstract Pattern getTilePattern();

    @Override
    public String toString() {
        return this.name;
    }

    protected String[] getTokens(Pattern pattern, String input, Map<Integer, String> replacements) {
        String[] tokens = null;
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) {
            int count = matcher.groupCount();
            tokens = new String[count];
            for (int i = 0; i < tokens.length; i++) {
                if (replacements != null && replacements.containsKey(i)) {
                    tokens[i] = replacements.get(i);
                } else {
                    tokens[i] = matcher.group(i + 1);
                }
            }
        } else {
            throw new RuntimeException("Name doesn't match the specifications");
        }
        return tokens;
    }

    protected abstract boolean verifyProductName(String name);

    public abstract String getOrbit();
}
