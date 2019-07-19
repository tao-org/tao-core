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
	public boolean checkUserProcessingMemory(Principal principal, int memory) throws QuotaException {
		// always true
		return true;
	}

	@Override
	public int getAvailableCpus(Principal principal) throws QuotaException {
		// always unlimited
		return -1;
	}

	@Override
	public void updateUserInputQuota(Principal principal) throws QuotaException {
		// Nothing to do
	}

	@Override
	public void updateUserProcessingQuota(Principal principal) throws QuotaException {
		// Nothing to do
	}
}
