package ro.cs.tao.services.bridge.spring;

/**
 * Created by cosmin on 9/13/2017.
 */

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import ro.cs.tao.persistence.PersistenceManager;

/**
 *
 * Register this SpringContextBridge as a Spring Component.
 * @author cosmin
 *
 */
@Service
public class SpringContextBridge implements SpringContextBridgedServices<PersistenceManager>, ApplicationContextAware
{
    /**
     * The application context
     */
    private static ApplicationContext __applicationContext;

    /**
     * Default empty constructor
     */
    private SpringContextBridge()
    {
    }

    @Override
    public void setApplicationContext(final ApplicationContext arg0) throws BeansException
    {
        __applicationContext = arg0;
    }

    /**
     * A static method to lookup the SpringContextBridgedServices Bean in
     * the applicationContext. It is basically an instance of itself, which
     * was registered by the @Component annotation.
     *
     * @return the SpringContextBridgedServices, which exposes all the
     * Spring services that are bridged from the Spring context.
     */
    public static SpringContextBridgedServices<PersistenceManager> services() throws SpringBridgeException
    {
        if(__applicationContext != null) {
            return __applicationContext.getBean(SpringContextBridgedServices.class);
        }
        throw new SpringBridgeException("Application context not initialized!");
    }

    @Override
    public PersistenceManager getPersistenceManager()
    {
        return __applicationContext.getBean(PersistenceManager.class);
    }
}