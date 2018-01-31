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
package com.sun.grid.drmaa.impl.torque;

import com.sun.grid.drmaa.impl.SessionImpl;
import org.ggf.drmaa.JobTemplate;


/**
 * This class represents a remote job and its attributes.  It is used to
 * set up the environment for a job to be submitted.
 *
 * <h3>DRMMA Attributes</h3>
 *
 * <p>DRMAA job template attributes can be set from six different sources.  In
 * order of precedence, from lowest to highest, there are: options
 * set by DRMAA automatically by default, options set in the sge_request
 * file(s), options set in the script file, options set by the jobCategory
 * property, options set by the nativeSpecification property, and
 * options set through other DRMAA properties.</p>
 *
 * <p>By default DRMAA sets four options for all jobs.  They are
 * &quot;-p  0&quot;, &quot;-b yes&quot;, &quot;-shell no&quot;, and
 * &quot;-w e&quot;.  This means that by default, all jobs will have priority 0,
 * all jobs will be treated as binary, i.e. no scripts args will be parsed, all
 * jobs will be executed without a wrapper shell, and jobs which are
 * unschedulable will cause a submit error.</p>
 *
 * The sge_request file, found in the $SGE_ROOT/$SGE_CELL/common directory, may
 * contain options to be applied to all jobs.  The .sge_request file found in
 * the user's home directory or the current working directory may also contain
 * options to be applied to certain jobs.  See the sge_request(5) man page
 * for more information.</p>
 *
 * <p>If the sge_request file contains &quot;-b no&quot; or if the
 * nativeSpecification property is set and contains &quot;-b no&quot;, the
 * script file will be parsed for in-line arguments. Otherwise, no script's args
 * will be interpreted.  See the qsub(1) man page for more information.</p>
 *
 * <p>If the jobCategory property is set, and the category it points to
 * exists in one of the qtask files, the options associated with that category
 * will be applied to the job template.  See the qtask(5) man page and
 * {@link #setJobCategory(String)} below for more information.</p>
 *
 * <p>If the nativeSpecification property is set, all options contained therein
 * will be applied to the job template.  See
 * {@link #setNativeSpecification(String)} below for more information.</p>
 *
 * <p>Other DRMAA attributes will override any previous settings.  For example,
 * if the sge_request file contains &quot;-j y&quot;, but the joinFiles
 * property is set to <code>false</code>, the ultimate result is that the input
 * and output files will remain separate.</p>
 *
 * <p>For various reasons, some options are silently ignored by DRMAA.  Setting
 * any of these options will have no effect.  The ignored options are:
 * &quot;-cwd&quot;, &quot;-help&quot;, &quot;-sync&quot;, &quot;-t&quot;,
 * &quot;-verify&quot;, &quot;-w w&quot;, and &quot;-w v&quot;.
 * The &quot;-cwd&quot; option can be reenabled by setting the environment
 * variable, SGE_DRMAA_ALLOW_CWD.  However, the &quot;-cwd&quot; option is not
 * thread safe and should not be used in a multi-threaded context.</p>
 *
 * <h3>Attribute Correlations</h3>
 *
 * <p>The following DRMAA attributes correspond to the following qsub
 * options:</p>
 *
 * <table>
 *  <tr><th>DRMAA Attribute</th><th>qsub Option</th></tr>
 *  <tr><td>remoteCommand</td><td>script file</td>
 *  <tr><td>args</td><td>script file arguments</td>
 *  <tr><td>jobSubmissionState = HOLD_STATE</td><td>-h</td>
 *  <tr><td>jobEnvironment</td><td>-v</td>
 *  <tr><td>workingDirectory = $PWD</td><td>-cwd</td>
 *  <tr><td>jobCategory</td><td>(qtsch qtask)<sup>*</sup></td>
 *  <tr><td>nativeSpecification</td><td>ALL<sup>*</sup></td>
 *  <tr><td>emailAddresses</td><td>-M</td>
 *  <tr><td>blockEmail = true</td><td>-m n</td>
 *  <tr><td>startTime</td><td>-a</td>
 *  <tr><td>jobName</td><td>-N</td>
 *  <tr><td>inputPath</td><td>-i</td>
 *  <tr><td>outputPath</td><td>-o</td>
 *  <tr><td>errorPath</td><td>-e</td>
 *  <tr><td>joinFiles</td><td>-j</td>
 *  <tr><td>transferFiles</td><td>(prolog and epilog)<sup>*</sup></td>
 * </table>
 *
 * <p><sup>*</sup> See the individual attribute setter description below</p>
 *
 * <p>The following attributes are unsupported by Grid Engine:</p>
 *
 * <ul>
 * <li>deadlineTime</li>
 * <li>hardWallclockTimeLimit</li>
 * <li>softWallclockTimeLimit</li>
 * <li>hardRunDurationTimeLimit</li>
 * <li>softRunDurationTimeLimit</li>
 * </ul>
 *
 * <p>Using the accessors for any of these attributes will result in an
 * UnsupportedAttributeException being thrown.</p>
 *
 * @author dan.templeton@sun.com
 * @see JobTemplate
 * @see org.ggf.drmaa.Session
 * @see SessionImpl
 * @see <a href="http://gridengine.sunsource.net/nonav/source/browse/~checkout~/gridengine/doc/htmlman/htmlman5/sge_request.html">sge_request(5)</a>
 * @see <a href="http://gridengine.sunsource.net/nonav/source/browse/~checkout~/gridengine/doc/htmlman/htmlman1/qsub.html">qsub(1)</a>
 * @see <a href="http://gridengine.sunsource.net/nonav/source/browse/~checkout~/gridengine/doc/htmlman/htmlman5/qtask.html">qtask(5)</a>
 * @since 0.5
 * @version 1.0
 */
public class JobTemplateImplTorque extends com.sun.grid.drmaa.impl.JobTemplateImpl {
    private SessionImplTorque session;

    private JobTemplateImplTorque(SessionImpl session, int id) {
        super(session, id);
        this.session = (SessionImplTorque)session;
        this.id = id;
    }
}
