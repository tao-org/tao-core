package ro.cs.tao.execution;

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;

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
public interface QuotaVerifier {
    /**
     * Allows the use of PersistenceManager class for database access
     * @param persistenceManager    The PersistenceManager reference
     * @see PersistenceManager
     */
    void setPersistenceManager(PersistenceManager persistenceManager);
    /**
     * Verifies if the input quota of the user was exceeded.
     * @param principal The user for which to verify the input quota
     * @return  <code>true</code> if the quota was not exceeded, <code>false</code> otherwise
     * @throws PersistenceException if the persistence manager is not set
     */
    boolean checkUserInputQuota(Principal principal) throws PersistenceException;
    /**
     * Verifies if the processing quota of the user was exceeded.
     * @param principal The user for which to verify the processing quota
     * @return  <code>true</code> if the quota was not exceeded, <code>false</code> otherwise
     * @throws PersistenceException if the persistence manager is not set
     */
    boolean checkUserProcessingQuota(Principal principal) throws PersistenceException;
    /**
     * Verifies if the processing resources assigned to a user were exceeded.
     * @param principal The user for which to verify the processing resources
     * @param job The current job from which a task is about to be executed.
     * @return  <code>true</code> if the quota was not exceeded, <code>false</code> otherwise
     * @throws PersistenceException if the persistence manager is not set
     */
    boolean checkUserProcessingResources(Principal principal, ExecutionJob job) throws PersistenceException;
}
