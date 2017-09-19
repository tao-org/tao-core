package ro.cs.tao.execution.local;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.Version;

import java.util.List;

/**
 * @author Cosmin Cara
 */
public class SessionImpl implements Session {
    @Override
    public void init(String contact) throws DrmaaException {

    }

    @Override
    public void exit() throws DrmaaException {

    }

    @Override
    public JobTemplate createJobTemplate() throws DrmaaException {
        return null;
    }

    @Override
    public void deleteJobTemplate(JobTemplate jt) throws DrmaaException {

    }

    @Override
    public String runJob(JobTemplate jt) throws DrmaaException {
        return null;
    }

    @Override
    public List runBulkJobs(JobTemplate jt, int start, int end, int incr) throws DrmaaException {
        return null;
    }

    @Override
    public void control(String jobId, int action) throws DrmaaException {

    }

    @Override
    public void synchronize(List jobIds, long timeout, boolean dispose) throws DrmaaException {

    }

    @Override
    public JobInfo wait(String jobId, long timeout) throws DrmaaException {
        return null;
    }

    @Override
    public int getJobProgramStatus(String jobId) throws DrmaaException {
        return 0;
    }

    @Override
    public String getContact() {
        return null;
    }

    @Override
    public Version getVersion() {
        return null;
    }

    @Override
    public String getDrmSystem() {
        return null;
    }

    @Override
    public String getDrmaaImplementation() {
        return null;
    }
}
