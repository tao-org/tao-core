/**
 * 
 */
package ro.cs.tao.quota;

import ro.cs.tao.configuration.ConfigurationManager;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

/**
 * Singleton class used to manage the user's quota values.
 * 
 * @author Lucian Barbulescu
 *
 */
public class UserQuotaManager {
	
	/** The quota manager. */
	private static QuotaManager _manager;
	
	
	static {
		// create the manager instance
        String quotaVerifierClass = ConfigurationManager.getInstance().getValue("tao.quota.manager", NullQuotaManager.class.getName());
        try {
        	_manager = (QuotaManager) Class.forName(quotaVerifierClass).getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            Logger.getLogger(UserQuotaManager.class.getName()).severe(String.format("QuotaManager class [%s] could not be instantiated. Reason: Class %s not found!",
                                                                               quotaVerifierClass, e.getMessage()));
            _manager = new NullQuotaManager();
        } catch (InstantiationException | IllegalAccessException e) {
            Logger.getLogger(UserQuotaManager.class.getName()).severe(String.format("QuotaManager class [%s] could not be instantiated. Reason: %s",
                                                                               quotaVerifierClass, e.getMessage()));
            _manager = new NullQuotaManager();
        } catch (InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Private constructor.
	 */
	private UserQuotaManager() {
	}
	
	/**
	 * Get the singleton's instance
	 * 
	 * @return the instance
	 */
	public static QuotaManager getInstance() {
		return _manager;
	}
}
