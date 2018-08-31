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
import org.ggf.drmaa.FileTransferMode;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.PartialTimestamp;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the DRMAA {@link JobTemplate} interface for local invocations.
 *
 * @author Cosmin Cara
 */
public class DefaultJobTemplate implements JobTemplate {
    @Override
    public void setRemoteCommand(String remoteCommand) throws DrmaaException {

    }

    @Override
    public String getRemoteCommand() throws DrmaaException {
        return null;
    }

    @Override
    public void setArgs(List args) throws DrmaaException {

    }

    @Override
    public List getArgs() throws DrmaaException {
        return null;
    }

    @Override
    public void setJobSubmissionState(int state) throws DrmaaException {

    }

    @Override
    public int getJobSubmissionState() throws DrmaaException {
        return 0;
    }

    @Override
    public void setJobEnvironment(Map env) throws DrmaaException {

    }

    @Override
    public Map getJobEnvironment() throws DrmaaException {
        return null;
    }

    @Override
    public void setWorkingDirectory(String wd) throws DrmaaException {

    }

    @Override
    public String getWorkingDirectory() throws DrmaaException {
        return null;
    }

    @Override
    public void setJobCategory(String category) throws DrmaaException {

    }

    @Override
    public String getJobCategory() throws DrmaaException {
        return null;
    }

    @Override
    public void setNativeSpecification(String spec) throws DrmaaException {

    }

    @Override
    public String getNativeSpecification() throws DrmaaException {
        return null;
    }

    @Override
    public void setEmail(Set email) throws DrmaaException {

    }

    @Override
    public Set getEmail() throws DrmaaException {
        return null;
    }

    @Override
    public void setBlockEmail(boolean blockEmail) throws DrmaaException {

    }

    @Override
    public boolean getBlockEmail() throws DrmaaException {
        return false;
    }

    @Override
    public void setStartTime(PartialTimestamp startTime) throws DrmaaException {

    }

    @Override
    public PartialTimestamp getStartTime() throws DrmaaException {
        return null;
    }

    @Override
    public void setJobName(String name) throws DrmaaException {

    }

    @Override
    public String getJobName() throws DrmaaException {
        return null;
    }

    @Override
    public void setInputPath(String inputPath) throws DrmaaException {

    }

    @Override
    public String getInputPath() throws DrmaaException {
        return null;
    }

    @Override
    public void setOutputPath(String outputPath) throws DrmaaException {

    }

    @Override
    public String getOutputPath() throws DrmaaException {
        return null;
    }

    @Override
    public void setErrorPath(String errorPath) throws DrmaaException {

    }

    @Override
    public String getErrorPath() throws DrmaaException {
        return null;
    }

    @Override
    public void setJoinFiles(boolean join) throws DrmaaException {

    }

    @Override
    public boolean getJoinFiles() throws DrmaaException {
        return false;
    }

    @Override
    public void setTransferFiles(FileTransferMode mode) throws DrmaaException {

    }

    @Override
    public FileTransferMode getTransferFiles() throws DrmaaException {
        return null;
    }

    @Override
    public void setDeadlineTime(PartialTimestamp deadline) throws DrmaaException {

    }

    @Override
    public PartialTimestamp getDeadlineTime() throws DrmaaException {
        return null;
    }

    @Override
    public void setHardWallclockTimeLimit(long hardWallclockLimit) throws DrmaaException {

    }

    @Override
    public long getHardWallclockTimeLimit() throws DrmaaException {
        return 0;
    }

    @Override
    public void setSoftWallclockTimeLimit(long softWallclockLimit) throws DrmaaException {

    }

    @Override
    public long getSoftWallclockTimeLimit() throws DrmaaException {
        return 0;
    }

    @Override
    public void setHardRunDurationLimit(long hardRunLimit) throws DrmaaException {

    }

    @Override
    public long getHardRunDurationLimit() throws DrmaaException {
        return 0;
    }

    @Override
    public void setSoftRunDurationLimit(long softRunLimit) throws DrmaaException {

    }

    @Override
    public long getSoftRunDurationLimit() throws DrmaaException {
        return 0;
    }

    @Override
    public Set getAttributeNames() throws DrmaaException {
        return null;
    }
}
