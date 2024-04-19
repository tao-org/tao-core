package ro.cs.tao.quota;

import java.security.Principal;

/**
 * Interface defining the main checks to be performed to enforce quota at various stages.
 * There are three types of quotas:
 * (1) Input quota: this relates to disk storage for input products that a user is allowed to download.
 *      If the framework runs on a platform that has locally an EO repository, it should be -1 (unlimited)
 * (2) Processing quota: this relates to disk storage for user's workflow results.
 * (3) Resources quota: this relates to the number of processing resources (machines or CPU+RAM) a user
 *      is allowed to use.
 *
 * @author Cosmin Cara
 */
public interface QuotaManager {
    /**
     * Verifies if the input quota of the user was exceeded.
     * @param principal The user for which to verify the input quota
     * @return  <code>true</code> if the quota was not exceeded, <code>false</code> otherwise
     * @throws QuotaException if the operation fails due to some error
     */
    boolean checkUserInputQuota(Principal principal) throws QuotaException;
    /**
     * Verifies if the input quota of the user will be exceeded by adding the new value.
     * @param principal The user for which to verify the input quota
     * @param addedQuota the additional value that will have an impact on the user's quota (in bytes)
     * @return  <code>true</code> if the quota was not exceeded, <code>false</code> otherwise
     * @throws QuotaException if the operation fails due to some error
     */
    boolean checkUserInputQuota(Principal principal, long addedQuota) throws QuotaException;
    /**
     * Verifies if the processing quota of the user was exceeded.
     * @param principal The user for which to verify the processing quota
     * @return  <code>true</code> if the quota was not exceeded, <code>false</code> otherwise
     * @throws QuotaException if the operation fails due to some error
     */
    boolean checkUserProcessingQuota(Principal principal) throws QuotaException;
    /**
     * Verifies if the user has enough RAM memory available.
     * @param principal The user for which to verify the processing resources
     * @param memory the amount of memory, in MB, required by the user
     * @return  <code>true</code> if the quota was not exceeded, <code>false</code> otherwise
     * @throws QuotaException if the operation fails due to some error
     */
    boolean checkUserProcessingMemory(Principal principal, int memory) throws QuotaException;
    
    /**
     * Get the available number of cpus for a user.
     * 
     * @param principal the user details 
     * @return the available number of cpu's for the user or -1 if no restrictions apply.
     * @throws QuotaException if the operation fails due to some error
     */
    int getAvailableCpus(Principal principal) throws QuotaException;
    
    /**
     * Update the user's input quota based on the public products that are assigned to him.
     * @param principal The user for which the operation must be performed
     * @throws QuotaException if the operation fails due to some error
     */
    void updateUserInputQuota(Principal principal) throws QuotaException;
    
    /**
     * Update the user's disk processing quota based on the content of its private folder
     * @param principal The user for which the operation must be performed
     * @throws QuotaException if the operation fails due to some error
     */
    void updateUserProcessingQuota(Principal principal) throws QuotaException;

    void updateUserCPU(Principal principal) throws QuotaException;
}
