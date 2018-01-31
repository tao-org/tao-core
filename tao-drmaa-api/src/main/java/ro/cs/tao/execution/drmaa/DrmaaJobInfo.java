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
package ro.cs.tao.execution.drmaa;

import org.ggf.drmaa.JobInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides information about a completed Grid Engine job.
 * @see org.ggf.drmaa.JobInfo
 * @author  dan.templeton@sun.com
 * @since 0.5
 * version 1.0
 */
public class DrmaaJobInfo implements JobInfo {
    protected final int exited;
    protected final int signaled;
    protected final int coredump;
    protected final int aborted;
    protected final int exitStatus;
    protected final String signal;
    protected final String jobId;
    protected final Map resources;
    
    /**
     * Creates a new instance of DrmaaJobInfo
     * @param jobId the job id string
     * @param exited non-zero if the process terminated normally
     * @param signaled non-zero if the process terminated because it received a signal that was not handled
     * @param coredump non-zero if the process terminated and produced a core dump
     * @param aborted non-zero if the process was aborted
     * @param resourceUsage an array of name=value resource usage pairs
     * @param exitStatus the low-order 8 bits of the process exit status
     * @param signal the string description of the terminating signal
     */
    public DrmaaJobInfo(String jobId, int exited, int signaled, int coredump, int aborted, String[] resourceUsage, int exitStatus, String signal) {
        this.jobId = jobId;
        this.exited = exited;
        this.signaled = signaled;
        this.coredump = coredump;
        this.aborted = aborted;
        this.exitStatus = exitStatus;
        this.resources = nameValuesToMap(resourceUsage);
        this.signal = signal;
    }
    
    protected static Map nameValuesToMap(String[] nameValuePairs) {
        Map map = null;

        if (nameValuePairs != null) {
            map = new HashMap();

            for (int count = 0; count < nameValuePairs.length; count++) {
                if (nameValuePairs[count] == null) {
                   continue;
                }
                int equals = nameValuePairs[count].indexOf('=');
                if (equals < 0) {
                   continue;
                }
                map.put(nameValuePairs[count].substring(0, equals), nameValuePairs[count].substring(equals + 1));
            }
        }

        return map;
    }
    
    public int getExitStatus() {
        if (!hasExited()) {
            throw new IllegalStateException();
        }

        return exitStatus;
    }
    
    /**
     * If hasSignaled() returns true, this method returns a representation of
     * the signal that caused the termination of the job. For signals declared
     * by POSIX or otherwise known to Grid Engine, the symbolic names are
     * returned (e.g., SIGABRT, SIGALRM).<BR>
     * For signals not known by Grid Engine, the string &quot;unknown
     * signal&quot; is returned.
     * @return the name of the terminating signal
     */
    public String getTerminatingSignal() {
        if (!hasSignaled()) {
            throw new IllegalStateException();
        }

        return signal;
    }
    
    public boolean hasCoreDump() {
        return coredump != 0;
    }
    
    public boolean hasExited() {
        return exited != 0;
    }
    
    public boolean hasSignaled() {
        return signaled != 0;
    }

    public boolean wasAborted() {
        return aborted != 0;
    }

    public String getJobId() {
        return jobId;
    }
    
    public Map getResourceUsage() {
        return resources;
    }
}
