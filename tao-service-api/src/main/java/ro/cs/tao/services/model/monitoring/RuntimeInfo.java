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

package ro.cs.tao.services.model.monitoring;

public class RuntimeInfo {
    // processor
    private double cpuTotal;
    // memory
    private long availableMemory;
    private long totalMemory;
    private MemoryUnit memoryUnit;
    // disk
    private long diskUsed;
    private long diskTotal;
    private MemoryUnit diskUnit;

    public double getCpuTotal() {
        return cpuTotal;
    }

    public void setCpuTotal(double cpuTotal) {
        this.cpuTotal = cpuTotal;
    }

    public long getAvailableMemory() {
        return availableMemory;
    }

    public void setAvailableMemory(long availableMemory) {
        this.availableMemory = availableMemory;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    public MemoryUnit getMemoryUnit() {
        return memoryUnit != null ? memoryUnit : MemoryUnit.MEGABYTE;
    }

    public void setMemoryUnit(MemoryUnit memoryUnit) {
        this.memoryUnit = memoryUnit;
    }

    public long getDiskUsed() {
        return diskUsed;
    }

    public void setDiskUsed(long diskUsed) {
        this.diskUsed = diskUsed;
    }

    public long getDiskTotal() {
        return diskTotal;
    }

    public void setDiskTotal(long diskTotal) {
        this.diskTotal = diskTotal;
    }

    public MemoryUnit getDiskUnit() {
        return this.diskUnit != null ? this.diskUnit : MemoryUnit.GIGABYTE;
    }

    public void setDiskUnit(MemoryUnit diskUnit) { this.diskUnit = diskUnit; }
}
