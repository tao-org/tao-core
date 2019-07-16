/**
 * 
 */
package ro.cs.tao.quota;

import java.security.Principal;

/**
 * Implementation that always validates quota constraints and performs no update.
 * 
 * @author Lucian Barbulescu
 *
 */
public class NullQuotaManager implements QuotaManager {

	@Override
	public boolean checkUserInputQuota(Principal principal) throws QuotaException {
		// always true
		return true;
	}

	@Override
	public boolean checkUserInputQuota(Principal principal, long addedQuota) throws QuotaException {
		// always true
		return true;
	}
	
	@Override
	public boolean checkUserProcessingQuota(Principal principal) throws QuotaException {
		// always true
		return true;
	}

	@Override
	public boolean checkUserProcessingResources(Principal principal) throws QuotaException {
		// always true
		return true;
	}

	@Override
	public void updateUserInputQuota(Principal principal) throws QuotaException {
		// Nothing to do
	}

	@Override
	public void updateUserProcessingQuota(Principal principal) throws QuotaException {
		// Nothing to do
	}

	@Override
	public boolean updateUserProcessingResources(Principal principal, long addedCpu, long addedMemory)
			throws QuotaException {
		// always true
		return true;
	}
}
