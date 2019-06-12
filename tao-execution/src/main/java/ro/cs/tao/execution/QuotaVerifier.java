package ro.cs.tao.execution;

import ro.cs.tao.execution.model.ExecutionJob;

import java.security.Principal;

/**
 * Interface defining the main checks to be performed to enforce quota at various stages.
 *
 * @author Cosmin Cara
 */
public interface QuotaVerifier {
    /**
     * Verifies if the input quota of the user was exceeded.
     * @param principal The user for which to verify the input quota
     * @return  <code>true</code> if the quota was not exceeded, <code>false</code> otherwise
     */
    boolean checkUserInputQuota(Principal principal);
    boolean checkUserProcessingQuota(Principal principal);
    boolean checkUserProcessingResources(Principal principal, ExecutionJob job);
}
