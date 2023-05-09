package ro.cs.tao.execution.drmaa;

/**
 * Extension interface for {@link org.ggf.drmaa.Session}.
 * Defines methods for getting the remote job exit code, output and, optionally, to perform any needed additional
 * cleanup required by the DRM.
 *
 * @author Cosmin Cara
 * @since 1.4.3
 */
public interface JobExitHandler {
    /**
     * Returns the exit code of a remote job.
     * @param jobId     The job identifier.
     */
    int getJobExitCode(String jobId);
    /**
     * Returns the output (logs) of a job
     * @param jobId     The job identifier
     */
    String getJobOutput(String jobId);
    /**
     * Performs any additional cleanup required by the DRM.
     * @param jobId     The job identifier
     */
    void cleanupJob(String jobId);
}
