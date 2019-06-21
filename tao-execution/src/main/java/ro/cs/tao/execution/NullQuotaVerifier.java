/**
 * 
 */
package ro.cs.tao.execution;

import java.security.Principal;

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;

/**
 * Implementation that always validates quota constraints.
 * 
 * @author Lucian Barbulescu
 *
 */
public class NullQuotaVerifier implements QuotaVerifier {

	@Override
	public void setPersistenceManager(PersistenceManager persistenceManager) {
		// Nothing to do
	}

	@Override
	public boolean checkUserInputQuota(Principal principal) throws PersistenceException {
		// Always true
		return true;
	}

	@Override
	public boolean checkUserProcessingQuota(Principal principal) throws PersistenceException {
		// Always true
		return true;
	}

	@Override
	public boolean checkUserProcessingResources(Principal principal, ExecutionJob job) throws PersistenceException {
		// Always true
		return true;
	}
}
