/*
 * Copyright (C) 2017 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.services.bridge.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import ro.cs.tao.persistence.PersistenceManager;

/**
 *
 * Register this SpringContextBridge as a Spring Component.
 * @author Cosmin Udroiu
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
