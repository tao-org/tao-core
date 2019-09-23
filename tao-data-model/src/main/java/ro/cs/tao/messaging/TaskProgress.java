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

/**
 * Encapsulates the execution progress of a task, and possible of its currently-executing sub-task.
 *
 * @author Cosmin Cara
 */
public class TaskProgress {
    private String name;
    private double progress;
    private SubTaskProgress subTaskProgress;

    public TaskProgress(String name) {
        this.name = name;
    }

    public TaskProgress(String name, double progress, String subTask, double subProgress) {
        this(name, progress, new SubTaskProgress(subTask, subProgress));
    }

    public TaskProgress(String name, double progress) {
        this(name, progress, null);
    }

    private TaskProgress(String name, double progress, SubTaskProgress subTaskProgress) {
        this.name = name;
        this.progress = progress;
        this.subTaskProgress = subTaskProgress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public SubTaskProgress getSubTaskProgress() {
        return subTaskProgress;
    }

    public void setSubTaskProgress(SubTaskProgress subTaskProgress) {
        this.subTaskProgress = subTaskProgress;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
