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

package ro.cs.tao.execution.model;

/**
 * Entity modelling the internal state of a TAO group component that represents a loop (i.e. the child components are
 * executed for each input of the group).
 */
public class LoopState {
    private int limit;
    private int current;

    public LoopState() { }

    public LoopState(int limit, int current) {
        this.limit = limit;
        this.current = current;
    }

    /**
     * The maximum value of the loop iterations.
     */
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }

    /**
     * The current loop counter
     */
    public int getCurrent() { return current; }
    public void setCurrent(int current) { this.current = current; }
}
