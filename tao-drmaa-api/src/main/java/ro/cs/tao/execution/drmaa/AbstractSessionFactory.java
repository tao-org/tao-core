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

import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import ro.cs.tao.configuration.ConfigurationManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * This class is used to create a AbstractSession instance.  In order to use the
 * Grid Engine binding, the $SGE_ROOT environment variable must be set, the
 * $SGE_ROOT/lib/drmaa.jar file must in included in the CLASSPATH environment
 * variable, and the $SGE_ROOT/lib/$ARCH directory must be included in the
 * library path, e.g. LD_LIBRARY_PATH.
 * @see org.ggf.drmaa.SessionFactory
 * @author dan.templeton@sun.com
 * @since 0.5
 * @version 1.0
 */
public abstract class AbstractSessionFactory extends SessionFactory {

    protected Session thisSession;
    private Logger logger = Logger.getLogger(getClass().getSimpleName());

    protected AbstractSessionFactory() {
        initLibrary();
    }

    public Session getSession() {
        synchronized (this) {
            if (thisSession == null) {
                thisSession = createSession();
            }
        }
        return thisSession;
    }

    protected abstract Session createSession();

    protected abstract String getJniLibraryName();

    private void initLibrary() {
        String path = ConfigurationManager.getInstance().getValue("native.library.path");
        if (path == null) {
            throw new MissingResourceException("Native library path is not defined",
                                               getClass().getSimpleName(), "native.library.path");
        }
        final Path libraryPath = Paths.get(path, getJniLibraryName());
        AccessController.doPrivileged((PrivilegedAction) () -> {
            try {
                if (!Files.exists(libraryPath)) {
                    Files.createDirectories(libraryPath.getParent());
                    logger.info(String.format("Copy library %s to %s",
                                              libraryPath.getFileName(),
                                              libraryPath.getParent()));
                    copyLibrary(libraryPath.getParent(), libraryPath.getFileName().toString());
                    fixUpPermissions(libraryPath);
                }
                System.load(libraryPath.toAbsolutePath().toString());
            } catch (Exception e){
                logger.severe(e.getMessage());
            }
            return null;
        });
    }

    private void copyLibrary(Path path, String libraryName) throws IOException {
        try (BufferedInputStream is = new BufferedInputStream(getClass().getResourceAsStream("/auxdata/" + libraryName))) {
            Files.copy(is, path.resolve(libraryName), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void setExecutablePermissions(Path pathName) throws IOException {
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
        Files.setPosixFilePermissions(pathName, permissions);
    }

    private void fixUpPermissions(Path destPath) throws IOException {
        /* Set execution permissions for all entries in path */
        try {
            setExecutablePermissions(destPath);
            if (Files.isDirectory(destPath)) {
                Stream<Path> files = Files.list(destPath);
                files.forEach(path -> {
                    try {
                        fixUpPermissions(path);
                    } catch (IOException e) {
                        Logger.getLogger(AbstractSessionFactory.class.getSimpleName())
                                .severe(String.format("Failed to fix permissions on '%s'", path));
                    }
                });
            }
        } catch (IOException e) {
            Logger.getLogger(AbstractSessionFactory.class.getSimpleName())
                    .severe(String.format("Failed to fix permissions on '%s'", destPath));
        }
    }
}
