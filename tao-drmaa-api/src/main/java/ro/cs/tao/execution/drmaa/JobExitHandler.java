package ro.cs.tao.execution.drmaa;

public interface JobExitHandler {
    int getJobExitCode(String jobId);
    String getJobOutput(String jobId);
    void cleanupJob(String jobId);
}
