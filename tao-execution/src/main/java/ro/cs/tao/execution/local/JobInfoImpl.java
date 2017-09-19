package ro.cs.tao.execution.local;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;

import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class JobInfoImpl implements JobInfo {
    private final Process process;
    private final int exited;
    private final int signaled;
    private final int coredump;
    private final int aborted;
    private final int exitStatus;
    private final String signal;
    private final String jobId;

    public JobInfoImpl(Process process) {
        this.process = process;
    }

    @Override
    public String getJobId() throws DrmaaException {
        Process p = new ProcessBuilder().start();
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
        return this.process.;
    }
}
