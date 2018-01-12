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
/*
 * SessionImplTest.java
 * JUnit based test
 *
 * Created on November 15, 2004, 10:41 AM
 */

package com.sun.grid.drmaa.torque;


import org.ggf.drmaa.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for SessionImpl class.
 */
public class SessionImplTest {
    private Session session = null;
    
    @Before
    public void setUp() {
        session = SessionFactory.getFactory().getSession();
    }
    
    @After
    public void tearDown() {
        session = null;
    }
    
    /** Test of init & exit methods, of class com.sun.grid.drmaa.SessionImpl. */
    @Test
    public void testInitExit() {
        System.out.println("testInitExit");
        
        this.initSession();
        this.exitSession();
    }
    
    private void initSession() {
        try {
            session.init(null);
        } catch (DrmaaException e) {
            fail("Unable to initialize session: " + e.getMessage());
        }
    }
    
    private void exitSession() {
        try {
            session.exit();
        } catch (DrmaaException e) {
            fail("Unable to exit session: " + e.getMessage());
        }
    }
    
    /** Test of getContact method, of class com.sun.grid.drmaa.SessionImpl. */
    @Test
    public void testGetContact() {
        System.out.println("testGetContact");

        assertNotNull(session.getContact());
        
        this.initSession();
        
        try {
            assertNotNull(session.getContact());
        } finally {
            this.exitSession();
        }
    }

    /** Test of getDRMSystem method, of class com.sun.grid.drmaa.SessionImpl. */
    @Test
    public void testGetDrmSystem() {
        System.out.println("testGetDrmSystem");
        
        String drmVersion = getVersionWithoutBuildNumber(session.getDrmSystem());
        assertNotNull(drmVersion);
               
        this.initSession();
        
        try {
            drmVersion = getVersionWithoutBuildNumber(session.getDrmSystem());
            assertNotNull(drmVersion);
        } finally {
            this.exitSession();
        }
    }

    /** Removes the appended build number if necessary */
    private String getVersionWithoutBuildNumber(final String version) {
        String fullVersion = "";
        int index = version.lastIndexOf("(");
        if (index > 0) {
            fullVersion = version.substring(0, index - 1);
        } else {
            fullVersion = version;
        }
        // Removes the product name from version string
        index = fullVersion.lastIndexOf(" ");
        if (index > 0) {
           return fullVersion.substring(index + 1);
        } else {
           return fullVersion;
        }
    }
    
    /** Test of getDRMAAImplementation method, of class 
     * com.sun.grid.drmaa.SessionImpl. */
    @Test
    public void testGetDrmaaImplementation() {
        System.out.println("testGetDrmaaImplementation");
                
        /**
         * In some rare cases the version strings can differ 
         * if some binaries are recompiled and others not.
         */
        String drmaa_version = getVersionWithoutBuildNumber(
                session.getDrmaaImplementation());
        assertNotNull(drmaa_version);
        
        this.initSession();
        drmaa_version = getVersionWithoutBuildNumber(
                session.getDrmaaImplementation());
        try {
            assertNotNull(drmaa_version);
        } finally {
            this.exitSession();
        }
    }
    
    /** Test of getVersion method, of class com.sun.grid.drmaa.SessionImpl. */
    @Test
    public void testGetVersion() {
        System.out.println("testGetVersion");

        Version v_0_5 = new Version(0, 5); 
        Version v_1_0 = new Version(1, 0);
        
        this.initSession();
        
        try {
            // get running drmaaj-wrapper version: 0.5 or 1.0
            Version current_version = session.getVersion();

            assertTrue(current_version.equals(v_0_5) 
                  || current_version.equals(v_1_0));  
        } finally {
            this.exitSession();
        }
    }
    
    /** Test of create|deleteJobTemplate method, of class 
     * com.sun.grid.drmaa.SessionImpl. */
    @Test
    public void testJobTemplate() {
        System.out.println("testJobTemplate");
        
        JobTemplate jt = null;
        
        this.initSession();
        
        try {
            try {
                jt = session.createJobTemplate();
            } catch (DrmaaException e) {
                fail("Unable to create job template: " + e.getMessage());
            }
            
            assertNotNull(jt);
            assertTrue(jt instanceof JobTemplateImpl);
            
            try {
                session.deleteJobTemplate(jt);
            } catch (InvalidJobTemplateException e) {
                fail("Unable to delete job template: " + e.getMessage());
            } catch (DrmaaException e) {
                fail("Unable to create job template: " + e.getMessage());
            }
            
            try {
                session.deleteJobTemplate(jt);
                fail("Able to delete job template twice");
            } catch (InvalidJobTemplateException e) {
                /* Don't care */
            } catch (DrmaaException e) {
                fail("Unable to delete job template: " + e.getMessage());
            }
        } finally {
            this.exitSession();
        }
    }
}
