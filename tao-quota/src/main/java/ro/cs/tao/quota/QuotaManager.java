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
     * Verifies if the processing quota of the user was exceeded.
     * @param principal The user for which to verify the processing quota
     * @return  <code>true</code> if the quota was not exceeded, <code>false</code> otherwise
     * @throws QuotaException if the operation fails due to some error
     */
    boolean checkUserProcessingQuota(Principal principal) throws QuotaException;
    /**
     * Verifies if the processing resources assigned to a user were exceeded.
     * @param principal The user for which to verify the processing resources
     * @return  <code>true</code> if the quota was not exceeded, <code>false</code> otherwise
     * @throws QuotaException if the operation fails due to some error
     */
    boolean checkUserProcessingResources(Principal principal) throws QuotaException;
    
    /**
     * Update the user's input quota.
     * @param principal The user for which the operation must be performed
     * @param addedQuota The quota value to add, in bytes. It can be negative. If 0 is used, the quota is re-computed, 
     * @return <code>true</code> if by adding the  value the quota is not exceeded, <code>false</code> otherwise
     * @throws QuotaException if the operation fails due to some error
     */
    boolean updateUserInputQuota(Principal principal, long addedQuota) throws QuotaException;
    
    /**
     * Update the user's disk processing quota
     * @param principal The user for which the operation must be performed
     * @param addedQuota The quota value to add, in bytes. It can be negative. If 0 is used, the quota is re-computed, 
     * @return <code>true</code> if by adding the  value the quota is not exceeded, <code>false</code> otherwise
     * @throws QuotaException if the operation fails due to some error
     */
    boolean updateUserProcessingQuota(Principal principal, long addedQuota) throws QuotaException;
    
    /**
     * Update the user's processing resources quota.
     * @param principal The user for which the operation must be performed
     * @param addedCpu The number of CPUs to add. It can be negative.
     * @param addedMemory The used memory, in MB. It can be negative
     * @return <code>true</code> if by adding the  value the quota is not exceeded, <code>false</code> otherwise
     * @throws QuotaException if the operation fails due to some error
     */
    boolean updateUserProcessingResources(Principal principal, long addedCpu, long addedMemory) throws QuotaException;
}
