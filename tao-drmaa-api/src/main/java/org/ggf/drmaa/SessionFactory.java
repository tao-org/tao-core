/*___INFO__MARK_BEGIN__*/
/*************************************************************************
 *
 *  The Contents of this file are made available subject to the terms of
 *  the Sun Industry Standards Source License Version 1.2
 *
 *  Sun Microsystems Inc., March, 2001
 *
 *
 *  Sun Industry Standards Source License Version 1.2
 *  =================================================
 *  The contents of this file are subject to the Sun Industry Standards
 *  Source License Version 1.2 (the "License"); You may not use this file
 *  except in compliance with the License. You may obtain a copy of the
 *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
 *
 *  Software provided under this License is provided on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
 *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 *  See the License for the specific provisions governing your rights and
 *  obligations concerning the Software.
 *
 *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
 *
 *   Copyright: 2001 by Sun Microsystems, Inc.
 *
 *   All Rights Reserved.
 *
 ************************************************************************/
/*___INFO__MARK_END__*/
package org.ggf.drmaa;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.drmaa.Environment;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;


/**
 * This class is used to retrieve a Session instance tailored to the DRM and
 * DRMAA implementation in use.  The factory will use the
 * org.ggf.SessionFactory property to discover the DRM-specific Session
 * implementation class.
 *
 * <p>Example:</p>
 *
 * <pre>public static void main(String[] args) throws Exception {
 *   SessionFactory factory = SessionFactory.getFactory();
 *   Session session = factory.getSession();
 *
 *   session.init(&quot;&quot;);
 *   session.exit();
 * }
 * </pre>
 * @author dan.templeton@sun.com
 * @see Session
 * @since 0.5
 * @version 1.0
 */
public abstract class SessionFactory {
    /**
     * Right now, only one SessionFactory can exist at a time.  This is that
     * session factory.
     */
    private static Map<Environment, SessionFactory> thisFactories = null;
    /**
     * The name of the property used to find the Session implementation
     * class name.
     */
    private static final String SESSION_PROPERTY =
            "org.ggf.drmaa.local.SessionFactory";

    /**
     * Gets a Session instance appropriate for the DRM in use.
     * @return a Session instance appropriate for the DRM in use
     */
    public abstract org.ggf.drmaa.Session getSession();

    public abstract Environment getEnvironment();

    /**
     * Gets a SessionFactory instance appropriate for the DRM in use.  This
     * method uses the org.ggf.SessionFactory property to find
     * the appropriate class.  It looks first in the system properties.  If the
     * property is not present, the method looks in
     * $java.home/lib/drmaa.properties.  If the property still isn't found, the
     * method will search the classpath for a
     * META-INF/services/org.ggf.SessionFactory resource.  If the
     * property still has not been found, the method throws an Error.
     * @return a SessionFactory instance appropriate for the DRM in use
     * @throws Error if an appropriate SessionFactory implementation could not
     * be found or instantiated
     */
    public static SessionFactory getFactory() {
        synchronized (SessionFactory.class) {
            if (thisFactories == null) {
                final SessionFactory factory = newFactory();
                thisFactories = new HashMap<>();
                thisFactories.put(factory.getEnvironment(), factory);
            }
        }
        return thisFactories.values().iterator().next();
    }

    public static SessionFactory getFactory(Environment env) {
        synchronized (SessionFactory.class) {
            if (thisFactories == null) {
                readFactories();
            }
        }
        return thisFactories.get(env);
    }

    public static Set<Environment> getEnvironments() {
        if (thisFactories == null) {
            readFactories();
        }
        return thisFactories != null ? thisFactories.keySet() : null;
    }

    /**
     * Creates a SessionFactory object appropriate for the DRM in use.  This
     * method uses the org.ggf.SessionFactory property to find
     * the appropriate class.  It looks first in the system properties.  If the
     * property is not present, the method looks in
     * $java.home/lib/drmaa.properties.  If the property still isn't found, the
     * method will search the classpath for a
     * META-INF/services/org.ggf.SessionFactory resource.  If the
     * property still has not been found, the method throws an Error.
     * @return a DRMAASession object appropriate for the DRM in use
     * @throws ConfigurationError if an appropriate SessionFactory
     * implementation could not be found or instantiated
     */
    private static SessionFactory newFactory() throws ConfigurationError {
        ClassLoader classLoader = findClassLoader();
        Exception e = null;
        
        // Use the system property first
        try {
            String systemProp = System.getProperty(SESSION_PROPERTY);
            
            if (systemProp != null) {
                return (SessionFactory)newInstance(systemProp, classLoader);
            }
        } catch (SecurityException se) {
            // If we get a security exception, treat it as failure and try the
            // next method
            e = se;
        }
        
        // try to read from $java.home/lib/drmaa.properties
        try {
            String javah = System.getProperty("java.home");
            String configFile = javah + File.separator + "lib" +
                    File.separator + "drmaa.properties";
            File f = new File(configFile);
            
            if (f.exists()) {
                Properties props = new Properties();
                
                props.load(new FileInputStream(f));
                
                String className = props.getProperty(SESSION_PROPERTY);
                
                return (SessionFactory)newInstance(className, classLoader);
            }
        } catch (SecurityException se ) {
            // If we get a security exception, treat it as failure and try the
            // next method
            e = se;
        } catch (IOException ie) {
            // If we get an I/O exception, treat it as failure and try the next
            // method
            e = ie;
        }
        SessionFactory factory = null;
        String className = ConfigurationManager.getInstance().getValue("tao.drmaa.sessionfactory");
        // try to find services in properties or CLASSPATH
        try {
            if (className != null && !className.isEmpty()) {
                factory = (SessionFactory)Class.forName(className).getConstructor().newInstance();
                Logger.getLogger(SessionFactory.class.getName()).info("Class " + className + " is loaded");
            } else {
                final ServiceRegistry<SessionFactory> registry =
                        ServiceRegistryManager.getInstance().getServiceRegistry(SessionFactory.class);
                if (registry != null) {
                    final Set<SessionFactory> services = registry.getServices();
                    if (services != null && !services.isEmpty()) {
                        factory = services.iterator().next();
                    }
                }
            }
            return factory;
        } catch (Exception ex) {
            //Ignore exceptions here and let the config error be thrown
            e = ex;
        }

        throw new ConfigurationError("Provider for " + className +
                " cannot be found", e);
    }

    private static void readFactories() throws ConfigurationError {
        ClassLoader classLoader = findClassLoader();
        Exception e = null;
        try {
            final ServiceRegistry<SessionFactory> registry =
                    ServiceRegistryManager.getInstance().getServiceRegistry(SessionFactory.class);
            String className = ConfigurationManager.getInstance().getValue("tao.drmaa.sessionfactory");
            if (registry != null) {
                final Set<SessionFactory> services = registry.getServices();
                for (SessionFactory factory : services) {
                    if (thisFactories == null) {
                        thisFactories = new HashMap<>();
                    }
                    final Environment environment = factory.getEnvironment();
                    if (environment != Environment.DEFAULT || factory.getClass().getName().equals(className)) {
                        thisFactories.put(environment, factory);
                    }
                }
            }
        } catch (Exception ex) {
            throw new ConfigurationError(ex.getMessage(), ex);
        }
    }
    
    /**
     * Figure out which ClassLoader to use.  For JDK 1.2 and later use the
     * context ClassLoader if possible.  Note: we defer linking the class
     * that calls an API only in JDK 1.2 until runtime so that we can catch
     * LinkageError so that this code will run in older non-Sun JVMs such
     * as the Microsoft JVM in IE.
     * @throws ConfigurationError thrown if the classloader cannot be found or
     * loaded
     * @return an appropriate ClassLoader
     */
    private static ClassLoader findClassLoader() {
        ClassLoader classLoader = null;
        
        try {
            // Construct the name of the concrete class to instantiate
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (LinkageError le) {
            // Assume that we are running JDK 1.1, use the current ClassLoader
            classLoader = SessionFactory.class.getClassLoader();
        } catch (Exception ex) {
            // Something abnormal happened so throw an error
            throw new ConfigurationError(ex.toString(), ex);
        }
        
        return classLoader;
    }
    
    /**
     * Create an instance of a class using the specified ClassLoader.
     * @param className The name of the class to be used to create the object
     * @param classLoader the classloader to use to create the object
     * @throws ConfigurationError thrown is the class cannot be instantiated
     * @return an instance of the given class
     */
    private static Object newInstance(String className, ClassLoader classLoader)
            throws ConfigurationError {
        try {
            Class spiClass;
            
            if (classLoader == null) {
                spiClass = Class.forName(className);
            } else {
                spiClass = classLoader.loadClass(className);
            }
            
            return spiClass.getConstructor().newInstance();
        } catch (ClassNotFoundException ex) {
            throw new ConfigurationError("Provider " + className +
                    " not found", ex);
        } catch (Exception ex) {
            throw new ConfigurationError("Provider " + className +
                    " could not be instantiated: " + ex,
                    ex);
        }
    }
    
    /**
     * Error used to indicate trouble loading the needed classes.  Note that
     * this class is private, meaning that it is only catchable as Error outside
     * of the SessionFactory class.
     */
    private static class ConfigurationError extends Error {
        /**
         * The Exception which caused this Exception
         */
        private final Exception exception;
        
        /**
         * Construct a new instance with the specified detail string and
         * exception.
         * @param msg the error message
         * @param ex the original Exception which caused this Exception
         */
        ConfigurationError(String msg, Exception ex) {
            super(msg);
            this.exception = ex;
        }
        
        /**
         * Get the Exception which caused this Exception
         * @return the Exception which caused this Exception
         */
        Exception getException() {
            return exception;
        }
    }
    
    /**
     * Privileged action used to load a factory implementation.  This class
     * allows the DRMAA library to be granted the required security permissions
     * without having to grant those permission to the user's application.
     */
    /*private static class NewFactoryAction implements PrivilegedAction {
        *//**
         * Create a new factory.
         * @return a new factory
         *//*
        public Object run() {
            return newFactory();
        }
    }*/
}
