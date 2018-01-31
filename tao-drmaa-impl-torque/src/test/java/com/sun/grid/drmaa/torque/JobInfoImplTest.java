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
 * JobInfoImplTest.java
 * JUnit based test
 *
 * Created on November 15, 2004, 10:41 AM
 */

package com.sun.grid.drmaa.torque;

import org.ggf.drmaa.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author dan.templeton@sun.com
 */
public class JobInfoImplTest {
    private static final String SIGTERM = "SIGTERM";
    private static final String SIGSEGV = "SIGSEGV";
    private JobInfo ji_ok = null;
    private JobInfo ji_sig = null;
    private JobInfo ji_core = null;
    private JobInfo ji_abort = null;

    @Before
    public void setUp() {
        // JobInfoImpl(String jobId, int exited, int signaled, int coredump, int aborted, String[] resourceUsage, int exitStatus, String signal)
        ji_ok = new JobInfoImpl("12345", 1, 0, 0, 0,
                new String[]{"cpu_time=1000", "mem_usage=1024"},
                0, null);
        ji_sig = new JobInfoImpl("12346", 0, 1, 0, 0,
                new String[]{"cpu_time=100", "mem_usage=1024"},
                145, SIGTERM);
        ji_core = new JobInfoImpl("12348", 0, 0, 1, 0,
                new String[]{"cpu_time=100", "mem_usage=2048"},
                139, SIGSEGV);
        ji_abort = new JobInfoImpl("12347", 0, 0, 0, 1,
                new String[]{}, 1, null);
    }

    @After
    public void tearDown() {
        ji_ok = null;
        ji_sig = null;
        ji_core = null;
        ji_abort = null;
    }

    /**
     * Test of getExitStatus method, of class com.sun.grid.drmaa.JobInfoImpl.
     */
    @Test
    public void testGetExitStatus() throws DrmaaException {
        System.out.println("testGetExitStatus");

        assertEquals(0, ji_ok.getExitStatus());
    }

    /**
     * Test of getTerminatingSignal method, of class com.sun.grid.drmaa.JobInfoImpl.
     */
    @Test
    public void testGetTerminatingSignal() throws DrmaaException {
        System.out.println("testGetTerminatingSignal");

        assertEquals(SIGTERM, ji_sig.getTerminatingSignal());
    }

    /**
     * Test of hasCoreDump method, of class com.sun.grid.drmaa.JobInfoImpl.
     */
    @Test
    public void testHasCoreDump() throws DrmaaException {
        System.out.println("testHasCoreDump");

        assertFalse(ji_ok.hasCoreDump());
        assertFalse(ji_sig.hasCoreDump());
        assertTrue(ji_core.hasCoreDump());
        assertFalse(ji_abort.hasCoreDump());
    }

    /**
     * Test of hasExited method, of class com.sun.grid.drmaa.JobInfoImpl.
     */
    @Test
    public void testHasExited() throws DrmaaException {
        System.out.println("testHasExited");

        assertTrue(ji_ok.hasExited());
        assertFalse(ji_sig.hasExited());
        assertFalse(ji_core.hasExited());
        assertFalse(ji_abort.hasExited());
    }

    /**
     * Test of hasSignaled method, of class com.sun.grid.drmaa.JobInfoImpl.
     */
    @Test
    public void testHasSignaled() throws DrmaaException {
        System.out.println("testHasSignaled");

        assertFalse(ji_ok.hasSignaled());
        assertTrue(ji_sig.hasSignaled());
        assertFalse(ji_core.hasSignaled());
        assertFalse(ji_abort.hasSignaled());
    }

    /**
     * Test of wasAborted method, of class com.sun.grid.drmaa.JobInfoImpl.
     */
    @Test
    public void testWasAborted() throws DrmaaException {
        System.out.println("testWasAborted");

        assertFalse(ji_ok.wasAborted());
        assertFalse(ji_sig.wasAborted());
        assertFalse(ji_core.wasAborted());
        assertTrue(ji_abort.wasAborted());
    }
}
