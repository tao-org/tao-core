/**
 * 
 */
package ro.cs.tao.execution.monitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.logging.Logger;

import ro.cs.tao.component.SystemVariable;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.execution.DefaultQuotaVerifier;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.NullQuotaVerifier;
import ro.cs.tao.execution.QuotaVerifier;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Topics;
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
	
	/** The quota verifier. */
	private QuotaVerifier verifier;
	
	
	static {
		_instance = new UserQuotaManager();
	}
	
	/**
	 * Private constructor.
	 */
	private UserQuotaManager() {
		this.persistenceManager = SpringContextBridge.services().getService(PersistenceManager.class);
		
		// create the verifier instance
        String quotaVerifierClass = ConfigurationManager.getInstance().getValue("tao.quota.verifier", DefaultQuotaVerifier.class.getName());
        try {
            verifier = (QuotaVerifier) Class.forName(quotaVerifierClass).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            Logger.getLogger(NodeManager.class.getName()).severe(String.format("QuotaVerifier class [%s] could not be instantiated. Reason: %s",
                                                                               quotaVerifierClass, e.getMessage()));
            verifier = new NullQuotaVerifier();
        }
        verifier.setPersistenceManager(persistenceManager);
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

    /**
     * Update the user's input quota by computing the size of its input products.
     */
    public void updateUserInputQuota() {
    	final Principal principal = SessionStore.currentContext().getPrincipal();
    	
    	final User user = persistenceManager.findUserByUsername(principal.getName());
        if (user == null) {
            throw new ExecutionException(String.format("User '%s' not found", principal.getName()));
        }
        
        // nothing to do if the user has no limit on the input disk quota 
        if (user.getInputQuota() == -1) {
        	return;
        }

        // get the location of the shared folder
        final String location = Paths.get(SystemVariable.SHARED_WORKSPACE.value()).toUri().toString();
        
        // compute the size of all public products assigned to the user 
        final long actualInputQuota = persistenceManager.getUserInputProductsSize(principal.getName(), location) / ((long) MemoryUnit.GIGABYTE.value());
        
        try {
    		// update the user info
    		persistenceManager.updateUserInputQuota(principal.getName(), actualInputQuota);
			
		} catch (PersistenceException e) {
			throw new ExecutionException(String.format("Cannot update input quota for the user '%s'. Reason: %s", principal.getName(), e.getMessage()), e);
		}
    }
    
    /**
     * Check if the user has reached its input quota. 
     * 
     * @param logger the logger that will receive the error message
     * @return true if the user still has quota available, false otherwise.
     */
    public boolean checkUserInputQuota(final Logger logger) {
		final Principal userPrincipal = SessionStore.currentContext().getPrincipal();
		try {
			if (!verifier.checkUserInputQuota(userPrincipal)) {
				// the allocated quota was reached. the execution cannot start
				// get the user's quotas
				final long[] quotas = persistenceManager.getUserDiskQuotas(userPrincipal.getName());
				
				// create the message
				final String msg = String.format("You cannot import more product because you have reached "
						+ "your input disk quota (defined quota: %dGB, used quota: %dGB). Please remove some input products "
						+ "to continue", quotas[0], quotas[1]);
				
				// publish the message to the ERROR topic and log it
				Messaging.send(SessionStore.currentContext().getPrincipal(), Topics.ERROR, this, msg);
				logger.info(msg);
				
				return false;
			}
		} catch (PersistenceException e) {
			throw new ExecutionException(String.format("Cannot check input quota for the user '%s'. Reason: %s", userPrincipal.getName(), e.getMessage()), e);
		}
		
		return true;
    }
    
    /**
     * Check if the user has reached its processing quota. 
     * 
     * @param logger the logger that will receive the error message
     * @return true if the user still has quota available, false otherwise.
     */
    public boolean checkUserProcessingQuota(final Logger logger) {
		final Principal userPrincipal = SessionStore.currentContext().getPrincipal();
		try {
			if (!verifier.checkUserProcessingQuota(userPrincipal)) {
				// the allocated quota was reached. the execution cannot start
				// get the user's quotas
				final long[] quotas = persistenceManager.getUserDiskQuotas(userPrincipal.getName());
				
				// create the message
				final String msg = String.format("The workflow execution cannot start because you have reached "
						+ "your processing disk quota (defined quota: %dGB, used quota: %dGB). Please delete some files from your "
						+ "workspace to continue", quotas[2], quotas[3]);
				
				// publish the message to the ERROR topic and log it
				Messaging.send(SessionStore.currentContext().getPrincipal(), Topics.ERROR, this, msg);
				logger.info(msg);
				
				return false;
			}
		} catch (PersistenceException e) {
			throw new ExecutionException(String.format("Cannot check disk processing quota for the user '%s'. Reason: %s", userPrincipal.getName(), e.getMessage()), e);
		}
		
		return true;
    }    
}
