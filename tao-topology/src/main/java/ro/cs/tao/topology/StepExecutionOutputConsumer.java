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
package ro.cs.tao.topology;

import ro.cs.tao.utils.executors.OutputConsumer;

/**
 * @author Cosmin Udroiu
 */
public class StepExecutionOutputConsumer implements OutputConsumer {
    private ToolInstallStep step;
    public StepExecutionOutputConsumer(ToolInstallStep step) {
        this.step = step;
    }
    public void consume(String message) {
        System.out.println(message);
        step.addExecutionMessage(message);
    }
}