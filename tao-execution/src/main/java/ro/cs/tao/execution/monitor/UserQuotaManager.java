/**
 * 
 */
package ro.cs.tao.execution.monitor;

import java.io.File;
import java.io.IOException;
import java.security.Principal;

import ro.cs.tao.component.SystemVariable;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.user.User;
import ro.cs.tao.utils.FileUtilities;

/**
 * Singleton class used to update the user's quota values.
 * 
 * @author Lucian Barbulescu
 *
 */
public class UserQuotaManager {
	
	/** The singleton instance. */
	private final static UserQuotaManager _instance;
	
	/** The persistence manager. */
	private PersistenceManager persistenceManager;
	
	static {
		_instance = new UserQuotaManager();
	}
	
	/**
	 * Private constructor.
	 */
	private UserQuotaManager() {
		this.persistenceManager = SpringContextBridge.services().getService(PersistenceManager.class);
	}
	
	/**
	 * Get the singleton's instance
	 * 
	 * @return the instance
	 */
	public static UserQuotaManager getInstance() {
		return _instance;
	}
	
    /**
     * Update the disk processing quota of a user by computing the size of its workspace folder
     */
    public void updateUserProcessingQuota() {
    	final Principal principal = SessionStore.currentContext().getPrincipal();
    	
    	final User user = persistenceManager.findUserByUsername(principal.getName());
        if (user == null) {
            throw new ExecutionException(String.format("User '%s' not found", principal.getName()));
        }
        
        // nothing to do if the user has no limit on the processing disk quota 
        if (user.getProcessingQuota() == -1) {
        	return;
        }
        	
        final String userWorkspace = SystemVariable.USER_WORKSPACE.value();
        try {
        	// compute used space in GB
			final long usedSpace = FileUtilities.folderSize(new File(userWorkspace).toPath()) / ((long) MemoryUnit.GIGABYTE.value());
			
			// update the user info
			persistenceManager.updateUserProcessingQuota(principal.getName(), usedSpace);
			
		} catch (IOException e) {
			throw new ExecutionException(String.format("Cannot compute used quota for the user '%s'. Reason: %s", principal.getName(), e.getMessage()), e);
		} catch (PersistenceException e) {
			throw new ExecutionException(String.format("Cannot update used quota for the user '%s'. Reason: %s", principal.getName(), e.getMessage()), e);
		}
    	
    }

	
}
