///*___INFO__MARK_BEGIN__*/
///*************************************************************************
// *
// *  The Contents of this file are made available subject to the terms of
// *  the Sun Industry Standards Source License Version 1.2
// *
// *  Sun Microsystems Inc., March, 2001
// *
// *
// *  Sun Industry Standards Source License Version 1.2
// *  =================================================
// *  The contents of this file are subject to the Sun Industry Standards
// *  Source License Version 1.2 (the "License"); You may not use this file
// *  except in compliance with the License. You may obtain a copy of the
// *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
// *
// *  Software provided under this License is provided on an "AS IS" basis,
// *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
// *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
// *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
// *  See the License for the specific provisions governing your rights and
// *  obligations concerning the Software.
// *
// *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
// *
// *   Copyright: 2001 by Sun Microsystems, Inc.
// *
// *   All Rights Reserved.
// *
// ************************************************************************/
///*___INFO__MARK_END__*/
///*
// * Issues.java
// * JUnit based test
// *
// * Created on June 10, 2008
// */
//
//import org.ggf.drmaa.DrmaaException;
//import org.ggf.drmaa.JobTemplate;
//import org.ggf.drmaa.Session;
//import org.ggf.drmaa.SessionFactory;
//import org.ggf.drmaa.Util;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.Collections;
//
//import static org.junit.Assert.assertNotEquals;
//import static org.junit.Assert.fail;
//
///**
// * When a job is in hold due to -hold_jid dependency this is not indicated
// * as STATE_SYSTEM_ON_HOLD (=17) by drmaa_job_ps(3) as it must be.
// * Instead STATE_QUEUED_ACTIVE (=16) is returned for such jobs.
// */
//public class Issue2608Test {
//    private static final String SLEEPER = "sleep";
//    private Session session;
//
//    /**
//     * Setup of test.
//     * @throws DrmaaException
//     */
//    @Before
//    public void setUp() throws DrmaaException {
//        session = SessionFactory.getFactory().getSession();
//        session.init("");
//    }
//
//    /**
//     * Tear down of test.
//     * @throws DrmaaException
//     */
//    @After
//    public void tearDown() throws DrmaaException {
//         try {
//            session.exit();
//        } catch (DrmaaException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    /**
//     * Test of issue 2608:
//     * - job hold due to -hold_jid is not indicated as
//     *   STATE_SYSTEM_ON_HOLD by drmaa_job_ps(3)
//     *
//     * @throws DrmaaException
//     */
//    @Test
//    public void test2608Test() throws DrmaaException {
//        System.out.println("testIssue2608");
//
//        /** create and submit 30 sec. sleeper job */
//        JobTemplate jt = this.createSleeperTemplate(20);
//        String job_id_1 = session.runJob(jt);
//
//        /** let the job wait till the other wait job finished */
//        if (Util.isTorqueSession(session)) {
//            jt.setNativeSpecification(" -W depend=afterok:" + job_id_1);
//        } else {
//            jt.setNativeSpecification(" -hold_jid " + job_id_1);
//        }
//
//        /** run second job which is waiting for the first */
//        String job_id_2 = session.runJob(jt);
//
//        /** make a short nip (15 sec) until job is scheduled */
//        try {
//            Thread.sleep(15000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        /** second job must be in suspended state and NOT in QUEUED ACTIVE */
//        int status = session.getJobProgramStatus(job_id_2);
//        assertNotEquals(status, Session.QUEUED_ACTIVE);
//
//        /** wait until first job has finished */
//        final int timeout = 20;
//        int time = 0;
//
//        while(session.getJobProgramStatus(job_id_1) != Session.DONE
//                && session.getJobProgramStatus(job_id_1) != Session.FAILED
//                && time < timeout) {
//            try {
//                /** make a short nip (5 sec) */
//                Thread.sleep(1000);
//            } catch (Exception e) {
//                fail("Failed while sleeping!"
//                         + e.getMessage());
//            }
//            time++;
//        }
//        time = 0;
//        /** wait until second job has finished */
//        while(session.getJobProgramStatus(job_id_2) != Session.DONE
//              && session.getJobProgramStatus(job_id_2) != Session.FAILED
//              && time < timeout) {
//
//            try {
//                /** make a short nip (1 sec) */
//                Thread.sleep(1000);
//            } catch (Exception e) {
//                fail("Failed while sleeping!"
//                         + e.getMessage());
//            }
//            time++;
//        }
//    }
//
//    /**
//     * Generates a JobTemplate for a sleeper job.
//     *
//     * @param sleep Time in second to sleep.
//     * @return JobTemplate
//     * @throws DrmaaException
//     */
//    private JobTemplate createSleeperTemplate(final int sleep)
//            throws DrmaaException {
//
//        JobTemplate jt = session.createJobTemplate();
//
//        jt.setRemoteCommand(SLEEPER);
//        jt.setArgs(Collections.singletonList(Integer.toString(sleep)));
//        jt.setOutputPath(":/dev/null");
//        jt.setErrorPath(":/dev/null");
//
//        return jt;
//    }
//
//}