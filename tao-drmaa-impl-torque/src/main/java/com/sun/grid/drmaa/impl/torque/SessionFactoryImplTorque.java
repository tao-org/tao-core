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

import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import ro.cs.tao.configuration.ConfigurationManager;

/**
 * This class is used to create a SessionImpl instance.  In order to use the
 * Grid Engine binding, the $SGE_ROOT environment variable must be set, the
 * $SGE_ROOT/lib/drmaa.jar file must in included in the CLASSPATH environment
 * variable, and the $SGE_ROOT/lib/$ARCH directory must be included in the
 * library path, e.g. LD_LIBRARY_PATH.
 * @see SessionFactory
 * @author dan.templeton@sun.com
 * @since 0.5
 * @version 1.0
 */
public class SessionFactoryImplTorque extends com.sun.grid.drmaa.impl.SessionFactoryImpl {

    private static ConfigurationManager config = null;
    private static String libraryPath = null;
    private static String libraryName = "libdrmaa-jni-torque.so";

    private static void copyLibrary() throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        byte[] buffer = new byte[1000];

        try{
            BufferedInputStream is = new BufferedInputStream(loader.getResourceAsStream(libraryName));
            OutputStream os = new BufferedOutputStream(new FileOutputStream(libraryPath + libraryName));
            while(is.read(buffer) != -1)
                os.write(buffer);
        }catch(IOException e){
            throw e;
        }
    }

    private static boolean libraryExists() throws Exception{
        try {
            File folder = new File(libraryPath);
            if (!folder.exists()) {
                /* Create library folder */
                folder.mkdir();
                System.out.println(new String("mkdir ") + libraryPath);
                return false;
            }
            else{
                /* file exists ?*/
                for(File file : folder.listFiles()){
                    if (file.getName().equals(libraryName)) return true;
                }
                return false;
            }
        }catch(Exception e){
            throw e;
        }
    }

    private static void setExecutablePermissions(Path pathName) throws IOException{
        /* Set execution permissions for path */
        Set<PosixFilePermission> permissions = new HashSet<>(Arrays.asList(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_READ,
                PosixFilePermission.OTHERS_EXECUTE
        ));
        try{
            Files.setPosixFilePermissions(pathName, permissions);
        }catch(IOException e){
            throw e;
        }
    }

    private static void fixUpPermissions(String strDestPath) throws IOException {
        /* Set execution permissions for all entries in path */
        Path destPath = Paths.get(strDestPath);
        try {
            setExecutablePermissions(destPath);
            if (Files.isDirectory(destPath)) {
                Stream<Path> files = Files.list(destPath);
                files.forEach(path -> {
                    try {
                        fixUpPermissions(path.toString());
                    }
                    catch (IOException e) {
                        System.out.println("Failed to fix permissions on " + path.toString());
                        return;
                    }
                });
            }
        }
        catch (IOException e) {
            System.out.println("Failed to fix permissions on " + destPath.toString());
        }
    }

    static {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                config = ConfigurationManager.getInstance();
                libraryPath = config.getValue("tao.jni.drmaa.librarypath");
                try {
                    if (!libraryExists()) {
                        System.out.println("Copy library " + libraryName +" to " + libraryPath);
                        copyLibrary();
                        fixUpPermissions(libraryPath);
                    }
                }catch(Exception e){
                    System.out.println(e.toString());
                }
                return null;
            }
        });
    }
    /**
     * Creates a new instance of SessionFactoryImpl.
     */
    public SessionFactoryImplTorque() {
    }
    
    public Session getSession() {
        synchronized (this) {
            if (thisSession == null) {
                thisSession = new SessionImplTorque();
            }
        }
        
        return thisSession;
    }
}
