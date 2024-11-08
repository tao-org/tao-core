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
package ro.cs.tao.execution.local;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import ro.cs.tao.utils.executors.Executor;

import java.util.Map;

/**
 * Implementation of the DRMAA {@link JobInfo} interface for local invocations.
 *
 * @author Cosmin Cara
 */
public class DefaultJobInfo implements JobInfo {
    private final Executor runner;
    private final String jobId;

    public DefaultJobInfo(String jobId, Executor runner) {
        this.jobId = jobId;
        this.runner = runner;
    }

    @Override
    public String getJobId() throws DrmaaException {
        //return String.valueOf(ProcessHelper.getPID(process));
        return jobId;
    }

    @Override
    public Map getResourceUsage() throws DrmaaException {
        return null;
    }

    @Override
    public boolean hasExited() throws DrmaaException {
        return this.runner.getReturnCode() == 0;
    }

    @Override
    public int getExitStatus() throws DrmaaException {
        return this.runner.getReturnCode();
    }

    @Override
    public boolean hasSignaled() throws DrmaaException {
        return !hasExited();
    }

    @Override
    public String getTerminatingSignal() throws DrmaaException {
        return null;
    }

    @Override
    public boolean hasCoreDump() throws DrmaaException {
        return false;
    }

    @Override
    public boolean wasAborted() throws DrmaaException {
        return this.runner.isStopped();
    }
}
