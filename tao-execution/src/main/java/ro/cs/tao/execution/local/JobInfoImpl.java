package ro.cs.tao.execution.local;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import ro.cs.tao.execution.simple.Executor;

import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class JobInfoImpl implements JobInfo {
    private final Executor runner;
    private String jobId;

    public JobInfoImpl(String jobId, Executor runner) {
        this.jobId = jobId;
        this.runner = runner;
    }

    @Override
    public String getJobId() throws DrmaaException {
        //return String.valueOf(PID.getPID(process));
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
        return this.runner.isCancelled() || this.runner.isStopped();
    }
}
