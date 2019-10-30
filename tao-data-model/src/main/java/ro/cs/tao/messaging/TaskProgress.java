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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Encapsulates the execution progress of a task, and possible of its currently-executing sub-task.
 *
 * @author Cosmin Cara
 */
public class TaskProgress {
    protected final String name;
    protected final String category;
    protected double progress;
    protected SubTaskProgress subTaskProgress;
    protected Map<String, String> info;

    public TaskProgress(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public TaskProgress(String name, String category, double progress, String subTask, double subProgress) {
        this(name, category, progress, new SubTaskProgress(subTask, subProgress));
    }

    public TaskProgress(String name, String category, double progress) {
        this(name, category, progress, null);
    }

    private TaskProgress(String name, String category, double progress, SubTaskProgress subTaskProgress) {
        this(name, category);
        this.progress = progress;
        this.subTaskProgress = subTaskProgress;
    }

    public String getName() { return name; }

    public String getCategory() { return category; }

    public double getProgress() { return progress; }

    public SubTaskProgress getSubTaskProgress() { return subTaskProgress; }

    public void addInfo(String key, String value) {
        if (this.info == null) {
            this.info = new HashMap<>();
        }
        this.info.put(key, value);
    }

    public Map<String, String> getInfo() { return info; }
    public void setInfo(Map<String, String> data) { this.info = data; }

    public String getInfo(String key) {
        return info != null ? info.get(key) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskProgress that = (TaskProgress) o;
        return name.equals(that.name) &&
                category.equals(that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category);
    }
}
