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
package ro.cs.tao.docker;

import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Descriptor for an application inside a Docker container.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "application")
public class Application {

    private String path;
    private String name;
    private String parallelFlagTemplate;
    private int memoryRequirements;

    @XmlElement(name = "path")
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    @XmlElement(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @XmlElement(name = "parallelFlagTemplate")
    public String getParallelFlagTemplate() { return parallelFlagTemplate; }
    public void setParallelFlagTemplate(String parallelFlagTemplate) { this.parallelFlagTemplate = parallelFlagTemplate; }

    @XmlElement(name = "memoryRequirements")
    public int getMemoryRequirements() { return memoryRequirements; }
    public void setMemoryRequirements(int memoryRequirements) { this.memoryRequirements = memoryRequirements; }

    public boolean hasParallelFlag() {
        return StringUtils.isNotEmpty(this.parallelFlagTemplate);
    }

    public Class<?> parallelArgumentType() {
        if (hasParallelFlag()) {
            String typeStr = this.parallelFlagTemplate.substring(this.parallelFlagTemplate.indexOf("<") + 1,
                                                                 this.parallelFlagTemplate.length() - 1).toLowerCase();
            switch (typeStr) {
                case "int":
                case "integer":
                    return Integer.class;
                case "long":
                    return Long.class;
                case "bool":
                case "boolean":
                    return Boolean.class;
                default:
                    throw new UnsupportedOperationException("Invalid argument type");
            }
        }
        return null;
    }

    public <T> String[] parallelArguments(Class<T> argType, T value) {
        if (hasParallelFlag()) {
            if (!argType.equals(parallelArgumentType())) {
                throw new UnsupportedOperationException("Invalid argument type");
            }
            String[] args = this.parallelFlagTemplate.split(" ");
            if (args.length > 2) {
                throw new UnsupportedOperationException("Invalid argument template");
            }
            if (args.length == 2) {
                args[1] = String.valueOf(value);
            } else {
                args[0] = args[0].substring(0, args[0].indexOf("<")) + String.valueOf(value);
            }
            return args;
        }
        return null;
    }
}
