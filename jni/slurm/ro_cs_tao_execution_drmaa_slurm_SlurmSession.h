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
/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class ro_cs_tao_execution_drmaa_AbstractSession */

#ifndef _Included_ro_cs_tao_execution_drmaa_AbstractSession
#define _Included_ro_cs_tao_execution_drmaa_AbstractSession
#ifdef __cplusplus
extern "C" {
#endif
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_SUSPEND
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_SUSPEND 0L
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_RESUME
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_RESUME 1L
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_HOLD
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_HOLD 2L
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_RELEASE
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_RELEASE 3L
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_TERMINATE
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_TERMINATE 4L
/* Inaccessible static: JOB_IDS_SESSION_ALL */
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_TIMEOUT_WAIT_FOREVER
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_TIMEOUT_WAIT_FOREVER -1LL
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_TIMEOUT_NO_WAIT
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_TIMEOUT_NO_WAIT 0LL
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_UNDETERMINED
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_UNDETERMINED 0L
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_QUEUED_ACTIVE
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_QUEUED_ACTIVE 16L
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_SYSTEM_ON_HOLD
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_SYSTEM_ON_HOLD 17L
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_USER_ON_HOLD
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_USER_ON_HOLD 18L
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_USER_SYSTEM_ON_HOLD
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_USER_SYSTEM_ON_HOLD 19L
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_RUNNING
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_RUNNING 32L
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_SYSTEM_SUSPENDED
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_SYSTEM_SUSPENDED 33L
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_USER_SUSPENDED
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_USER_SUSPENDED 34L
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_DONE
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_DONE 48L
#undef ro_cs_tao_execution_drmaa_slurm_SlurmSession_FAILED
#define ro_cs_tao_execution_drmaa_slurm_SlurmSession_FAILED 64L
/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeControl
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeControl
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeExit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeExit
  (JNIEnv *, jobject);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeGetContact
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeGetContact
  (JNIEnv *, jobject);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeGetDRMSInfo
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeGetDRMSInfo
  (JNIEnv *, jobject);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeGetJobProgramStatus
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeGetJobProgramStatus
  (JNIEnv *, jobject, jstring);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeInit
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeInit
  (JNIEnv *, jobject, jstring);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeRunBulkJobs
 * Signature: (IIII)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeRunBulkJobs
  (JNIEnv *, jobject, jint, jint, jint, jint);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeRunJob
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeRunJob
  (JNIEnv *, jobject, jint);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeSynchronize
 * Signature: ([Ljava/lang/String;JZ)V
 */
JNIEXPORT void JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeSynchronize
  (JNIEnv *, jobject, jobjectArray, jlong, jboolean);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeWait
 * Signature: (Ljava/lang/String;J)Lcom/sun/grid/drmaa/SGEJobInfo;
 */
JNIEXPORT jobject JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeWait
  (JNIEnv *, jobject, jstring, jlong);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeAllocateJobTemplate
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeAllocateJobTemplate
  (JNIEnv *, jobject);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeSetAttributeValue
 * Signature: (ILjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeSetAttributeValue
  (JNIEnv *, jobject, jint, jstring, jstring);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeSetAttributeValues
 * Signature: (ILjava/lang/String;[Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeSetAttributeValues
  (JNIEnv *, jobject, jint, jstring, jobjectArray);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeGetAttributeNames
 * Signature: (I)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeGetAttributeNames
  (JNIEnv *, jobject, jint);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeGetAttribute
 * Signature: (ILjava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeGetAttribute
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     ro_cs_tao_execution_drmaa_AbstractSession
 * Method:    nativeDeleteJobTemplate
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_ro_cs_tao_execution_drmaa_slurm_SlurmSession_nativeDeleteJobTemplate
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif