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
package ro.cs.tao.eodata.util;

import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for product helpers.
 *
 * @author Cosmin Cara
 */
public abstract class BaseProductHelper implements ProductHelper {
    protected Path path;
    protected String name;
    protected String id;
    protected String sensingDate;
    protected String processingDate;
    protected String version;
    protected String[] tokens;

    protected BaseProductHelper() {}

    public BaseProductHelper(String name) {
        setName(name);
    }

    public BaseProductHelper(Path productPath) {
        this(productPath.getFileName().toString());
        this.path = productPath;
    }

    /**
     * Returns the name of the product
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the product. If the name of the product doesn't pass the name validation rules,
     * it throws an exception
     * @param name  The name of the product
     */
    @Override
    public void setName(String name) {
        if (!verifyProductName(name)) {
            throw new IllegalArgumentException(String.format("The product name %s doesn't match the expected pattern", name));
        }
        this.name = name;
    }

    /**
     * Returns the concrete class of this helper.
     */
    @Override
    public Class<? extends ProductHelper> getHelperClass() { return getClass(); }

    /**
     * Returns the identifier of the product.
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Sets the identifier of the product.
     * @param id    The product identifier
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the version of the product.
     */
    @Override
    public String getVersion() { return version; }

    /**
     * Sets the version of the product
     * @param version   The product version
     */
    @Override
    public void setVersion(String version) { this.version = version; }

    /**
     * Returns the sensing date of the product.
     */
    @Override
    public String getSensingDate() { return sensingDate; }

    /**
     * Sets the sensing date of the product.
     * @param date  The sensing date, as string.
     */
    @Override
    public void setSensingDate(String date) { this.sensingDate = date; }

    @Override
    public String getProcessingDate() { return processingDate; }

    @Override
    public void setProcessingDate(String processingDate) { this.processingDate = processingDate; }

    /**
     * Returns the path of the product, relative to the product repository root.
     */
    @Override
    public String getProductRelativePath() {
        if (this.path != null) {
            return this.path.getParent().toString();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String[] getTokens(Pattern pattern) {
        if (this.tokens == null && this.name != null) {
            String[] tokens;
            Matcher matcher = pattern.matcher(this.name);
            if (matcher.matches()) {
                int count = matcher.groupCount();
                tokens = new String[count];
                for (int i = 0; i < tokens.length; i++) {
                    tokens[i] = matcher.group(i + 1);
                }
            } else {
                throw new RuntimeException("Name doesn't match the specifications");
            }
            return tokens;
        }
        return this.tokens;
    }

    @Override
    public String[] getTokens(Pattern pattern, String input, Map<Integer, String> replacements) {
        String[] tokens;
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

    @Override
    public boolean isIntended(String name) {
        return verifyProductName(name);
    }

    protected abstract boolean verifyProductName(String name);
}
