/*
 *
 *  * Copyright (C) 2018 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */
package ro.cs.tao;

/**
 * Callback interface for progress reporting of a single execution task.
 *
 * @author Cosmin Cara
 */
public interface ProgressListener {
    /**
     * Signals that an activity (task) has started.
     * @param taskName  The name of the activity.
     */
    default void started(String taskName) {
        System.out.println(taskName + " started");
    }
    /**
     * Signals that a sub-activity has started.
     * @param subTaskName The name of the sub-activity.
     */
    default void subActivityStarted(String subTaskName) {
        System.out.println(subTaskName + " started");
    }
    /**
     * Signals that a sub-activity has completed.
     * @param subTaskName The name of the sub-activity.
     */
    default void subActivityEnded(String subTaskName) {
        System.out.println(subTaskName + " completed");
    }
    /**
     * Signals that the current activity (task) has completed.
     */
    default void ended() { }

    /**
     * Signals the current progress of the activity.
     * @param progressValue The progress value (between 0 and 1).
     */
    default void notifyProgress(double progressValue) {
        System.out.printf("[%.2f%%]\r", progressValue * 100);
    }

    /**
     * Signals the progress of the current sub-activity.
     * @param subTaskName       The sub-activity name.
     * @param subTaskProgress   The sub-activity progress.
     */
    default void notifyProgress(String subTaskName, double subTaskProgress) {
        System.out.printf("[%s: %.2f%%]\r", subTaskName, subTaskProgress * 100);
    }

    /**
     * Signals the overall activity progress and the progress of the current sub-activity.
     * @param subTaskName       The main activity name.
     * @param subTaskProgress   The sub-activity progress.
     * @param overallProgress   The main activity progress.
     */
    default void notifyProgress(String subTaskName, double subTaskProgress, double overallProgress) {
        System.out.printf("[Overall progress: %.2f%%, %s progress: %.2f%%]\r", overallProgress * 100, subTaskName, subTaskProgress * 100);
    }
}
