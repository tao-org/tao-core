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
package ro.cs.tao.execution.monitor;

/**
 * @author Cosmin Cara
 */
public class Memory {
    private MemoryUnit memoryUnit;

    private long heapCommitted;
    private long heapInitial;
    private long heapMax;
    private long heapUsed;
    private long nonHeapCommitted;
    private long nonHeapInitial;
    private long nonHeapMax;
    private long nonHeapUsed;

    public Memory(MemoryUnit unit) { this.memoryUnit = unit; }

    public MemoryUnit getMemoryUnit() {
        return memoryUnit;
    }

    public long getHeapCommitted() {
        return heapCommitted / this.memoryUnit.value();
    }

    public void setHeapCommitted(long heapCommitted) {
        this.heapCommitted = heapCommitted;
    }

    public long getHeapInitial() {
        return heapInitial / this.memoryUnit.value();
    }

    public void setHeapInitial(long heapInitial) {
        this.heapInitial = heapInitial;
    }

    public long getHeapMax() {
        return heapMax / this.memoryUnit.value();
    }

    public void setHeapMax(long heapMax) {
        this.heapMax = heapMax;
    }

    public long getHeapUsed() {
        return heapUsed / this.memoryUnit.value();
    }

    public void setHeapUsed(long heapUsed) {
        this.heapUsed = heapUsed;
    }

    public long getNonHeapCommitted() {
        return nonHeapCommitted / this.memoryUnit.value();
    }

    public void setNonHeapCommitted(long nonHeapCommitted) {
        this.nonHeapCommitted = nonHeapCommitted;
    }

    public long getNonHeapInitial() {
        return nonHeapInitial / this.memoryUnit.value();
    }

    public void setNonHeapInitial(long nonHeapInitial) {
        this.nonHeapInitial = nonHeapInitial;
    }

    public long getNonHeapMax() {
        return nonHeapMax / this.memoryUnit.value();
    }

    public void setNonHeapMax(long nonHeapMax) {
        this.nonHeapMax = nonHeapMax;
    }

    public long getNonHeapUsed() {
        return nonHeapUsed / this.memoryUnit.value();
    }

    public void setNonHeapUsed(long nonHeapUsed) {
        this.nonHeapUsed = nonHeapUsed;
    }
}
