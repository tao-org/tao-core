package ro.cs.tao.execution.local;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;

import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class JobInfoImpl implements JobInfo {
    private final Process process;
    private int exited;
    private int signaled;
    private int coredump;
    private int aborted;
    private int exitStatus;
    private String signal;
    private String jobId;

    public JobInfoImpl(Process process) {
        this.process = process;
    }

    @Override
    public String getJobId() throws DrmaaException {
        return String.valueOf(PID.getPID(process));
    }

    @Override
    public Map getResourceUsage() throws DrmaaException {
        return null;
    }

    @Override
    public boolean hasExited() throws DrmaaException {
        return this.process.exitValue() == 0;
    }

    @Override
    public int getExitStatus() throws DrmaaException {
        return this.process.exitValue();
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
        return false;
    }
}
