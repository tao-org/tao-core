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
package ro.cs.tao.messaging;

import java.util.regex.Pattern;

/**
 * General topics handled by the registered event bus.
 * @author Cosmin Cara
 */
public class Topic {
    /**
     * Topic for informational messages / events.
     */
    public static final Topic INFORMATION = Topic.create("info");
    /**
     * Topic for warning messages / events.
     */
    public static final Topic WARNING = Topic.create("warn");
    /**
     * Topic for error messages / events.
     */
    public static final Topic ERROR = Topic.create("error");
    /**
     * Topic for signaling progress of various execution components.
     */
    public static final Topic PROGRESS = Topic.create("progress");
    /**
     * Special topic for signaling that an execution task has changed its state.
     */
    public static final Topic EXECUTION = Topic.create("execution.status.changed");
    /**
     * Special topic for signaling that a topology node was added or removed.
     */
    public static final Topic TOPOLOGY = Topic.create("topology");

    public static Topic create(String category) {
        return new Topic(category, null);
    }

    public static Topic create(String category, String tag) {
        return new Topic(category, tag);
    }

    public static Topic create(Topic parent, String tag) {
        return new Topic(parent.value(), tag);
    }

    public static Pattern getCategoryPattern(Topic topic) {
        return Pattern.compile(topic.getTag() == null ? "(.+)" + topic.getCategory() : topic.value());
    }

    private final String category;
    private final String tag;

    protected Topic(String category, String tag) {
        this.category = category;
        this.tag = tag;
    }

    public String getCategory() { return category; }
    public String getTag() { return tag; }

    public String value() {
        return (this.tag != null ? this.tag  + "." : "") + this.category;
    }

    public boolean isParentOf(Topic other) {
        return other.value().endsWith(value());
    }

    public boolean isParentOf(String other) {
        return other.endsWith(value());
    }

    public boolean isChildOf(Topic other) {
        return value().endsWith(other.value());
    }

    public boolean isChildOf(String other) {
        return value().endsWith(other);
    }

    @Override
    public String toString() { return value(); }
}
