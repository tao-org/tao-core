/*
 * Copyright (C) 2014-2015 CS-SI (foss-contact@thor.si.c-s.fr)
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

package ro.cs.eo.gdal.dataio;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.utils.StringUtilities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static org.apache.commons.lang.SystemUtils.IS_OS_UNIX;

/**
 * GDAL installer class for deploying GDAL binaries to the aux data dir.
 *
 * @author Cosmin Cara
 * @author Adrian Drăghici
 */
class GDALInstaller {

    private static final String PREFERENCE_KEY = "gdal.installer";
    private static final String CONFIG_FILE_NAME = "tao_gdal.properties";
    private static final Logger logger = Logger.getLogger(GDALInstaller.class.getName());

    private final Path gdalNativeLibrariesFolderPath;

    GDALInstaller(Path gdalNativeLibrariesFolderPath) {
        this.gdalNativeLibrariesFolderPath = gdalNativeLibrariesFolderPath;
    }

    /**
     * Fixes the permissions issue with executables on UNIX OS.
     *
     * @param destPath the target directory/executable file path
     * @throws IOException When IO error occurs
     */
    static void fixUpPermissions(Path destPath) throws IOException {
        try (Stream<Path> files = Files.list(destPath)) {
            files.forEach(path -> {
                if (Files.isDirectory(path)) {
                    try {
                        fixUpPermissions(path);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "GDAL configuration error: failed to fix permissions on " + path, e);
                    }
                } else {
                    setExecutablePermissions(path);
                }
            });
        }
    }

    /**
     * Sets required permissions for executables on UNIX OS.
     *
     * @param executablePathName the target executable file path
     */
    private static void setExecutablePermissions(Path executablePathName) {
        if (IS_OS_UNIX) {
            final Set<PosixFilePermission> permissions = new HashSet<>(Arrays.asList(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.GROUP_READ,
                    PosixFilePermission.GROUP_EXECUTE,
                    PosixFilePermission.OTHERS_READ,
                    PosixFilePermission.OTHERS_EXECUTE));
            try {
                Files.setPosixFilePermissions(executablePathName, permissions);
            } catch (IOException e) {
                // can't set the permissions for this file, eg. the file was installed as root
                // send a warning message, user will have to do that by hand.
                logger.log(Level.SEVERE, "Can't set execution permissions for executable " + executablePathName +
                        ". If required, please ask an authorised user to make the file executable.", e);
            }
        }
    }

    /**
     * Compares module version strings.
     *
     * @param currentModuleVersion the current module version string
     * @param savedModuleVersion   the saved module version string
     * @return comparision result index where:
     * >0 when current module version greater than saved module version
     * 0 when current module version same as saved module version
     * <0 when current module version lower than saved module version
     */
    private static int compareVersions(String currentModuleVersion, String savedModuleVersion) {
        final int[] moduleVersionFragments = parseVersion(currentModuleVersion);
        final int[] savedVersionFragments = parseVersion(savedModuleVersion);

        final int max = Math.max(moduleVersionFragments.length, savedVersionFragments.length);
        for (int i = 0; i < max; ++i) {
            final int d1 = (i < moduleVersionFragments.length) ? moduleVersionFragments[i] : 0;
            final int d2 = (i < savedVersionFragments.length) ? savedVersionFragments[i] : 0;
            if (d1 != d2) {
                return d1 - d2;
            }
        }
        return 0;
    }

    /**
     * Parses the module version string
     *
     * @param version the module version string
     * @return parsed module version array
     */
    private static int[] parseVersion(String version) {
        if (version.toLowerCase().contentEquals("unknown")) {
            return new int[]{};
        }
        final StringTokenizer tok = new StringTokenizer(version, ".", true);
        final int len = tok.countTokens();
        if (len % 2 == 0) {
            throw new NumberFormatException("Even number of pieces in a spec version: '" + version + "'");
        }
        final int[] digits = new int[len / 2 + 1];
        int index = 0;
        boolean expectingNumber = true;
        while (tok.hasMoreTokens()) {
            final String fragment = tok.nextToken().replaceAll("-SNAPSHOT", "");
            if (expectingNumber) {
                expectingNumber = false;
                final int piece = Integer.parseInt(fragment);
                if (piece < 0) {
                    throw new NumberFormatException("Spec version component '" + piece + "' is negative.");
                }
                digits[index++] = piece;
            } else {
                if (!".".equals(fragment)) {
                    throw new NumberFormatException("Expected dot in version '" + version + "'.");
                }
                expectingNumber = true;
            }
        }

        return digits;
    }

    /**
     * Fetches the saved module specification version from TAO GDAL config.
     *
     * @return the saved module specification version
     */
    private static String fetchSavedModuleSpecificationVersion(Path gdalNativeLibrariesFolderPath) {
        try {
            final Properties gdalConfig = new Properties();
            gdalConfig.load(Files.newBufferedReader(gdalNativeLibrariesFolderPath.resolve(CONFIG_FILE_NAME)));
            return gdalConfig.getProperty(PREFERENCE_KEY, null);
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * Sets the saved module specification version to TAO GDAL config.
     */
    private static void setSavedModuleSpecificationVersion(String newModuleSpecificationVersion, Path gdalNativeLibrariesFolderPath) {
        try {
            final Properties gdalConfig = new Properties();
            gdalConfig.setProperty(PREFERENCE_KEY, newModuleSpecificationVersion);
            gdalConfig.store(Files.newBufferedWriter(gdalNativeLibrariesFolderPath.resolve(CONFIG_FILE_NAME)), "");
        } catch (IOException ignored) {
        }
    }

    /**
     * Copies the environment variables native library used for access OS environment variables.
     *
     * @param gdalVersion the GDAL version to which JNI environment variables native library be installed
     * @throws IOException When IO error occurs
     */
    private static void copyEnvironmentVariablesNativeLibrary(GDALVersion gdalVersion) throws IOException {
        final Path evFilePath = gdalVersion.getEnvironmentVariablesFilePath();
        logger.fine("Copy the environment variables library file.");

        final URL libraryFileURLFromSources = gdalVersion.getEnvironmentVariablesFilePathFromSources();
        if (libraryFileURLFromSources != null) {
            logger.fine("The environment variables library file path on the local disk is '" + evFilePath + "' and the library file name from sources is '" + libraryFileURLFromSources + "'.");

            FileUtilities.copyFile(libraryFileURLFromSources, evFilePath);
        } else {
            throw new IllegalStateException("Unable to get environment variables libraryFileURLFromSources");
        }
    }

    /**
     * Copies the loader library used for load GDAL native library.
     *
     * @param gdalVersion the GDAL version to which loader library be installed
     * @throws IOException When IO error occurs
     */
    private static void copyLoaderLibrary(GDALVersion gdalVersion) throws IOException {
        final Path loaderFilePath = gdalVersion.getLoaderLibraryFilePath();
        logger.fine("Copy the loader library file.");

        final URL libraryFileURLFromSources = gdalVersion.getLoaderFilePathFromSources();
        if (libraryFileURLFromSources != null) {
            logger.fine("The loader library file path on the local disk is '" + loaderFilePath + "' and the library file name from sources is '" + libraryFileURLFromSources + "'.");

            FileUtilities.copyFile(libraryFileURLFromSources, loaderFilePath);
        } else {
            throw new IllegalStateException("Unable to get loader libraryFileURLFromSources");
        }
    }

    /**
     * Copies and extracts the GDAL distribution archive on specified directory.
     *
     * @param gdalDistributionRootFolderPath the GDAL distribution root directory for install
     * @param gdalVersion                    the GDAL version to which GDAL distribution be installed
     * @throws IOException When IO error occurs
     */
    private static void copyDistributionArchiveAndInstall(Path gdalDistributionRootFolderPath, GDALVersion gdalVersion) throws IOException {
        if (!Files.exists(gdalDistributionRootFolderPath)) {
            logger.fine("create the distribution root folder '" + gdalDistributionRootFolderPath + "'.");
            Files.createDirectories(gdalDistributionRootFolderPath);
        }
        logger.fine("The distribution root folder '" + gdalDistributionRootFolderPath + "' exists on the local disk.");
        final Path zipFilePath = gdalVersion.getZipFilePath();
        try {
            logger.fine("Copy the zip archive to folder '" + zipFilePath + "'.");
            final URL zipFileURLFromSources = gdalVersion.getZipFileURLFromSources();
            if (zipFileURLFromSources == null) {
                throw new ExceptionInInitializerError("No GDAL distribution drivers provided for this OS.");
            }
            FileUtilities.copyFile(zipFileURLFromSources, zipFilePath);

            logger.fine("Decompress the zip archive to folder '" + gdalDistributionRootFolderPath + "'.");
            installDistribution(zipFilePath, gdalDistributionRootFolderPath);
        } finally {
            try {
                Files.deleteIfExists(zipFilePath);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "GDAL configuration error: failed to delete the zip archive after decompression.", e);
            }
        }
    }

    /**
     * Fetches the current module specification version from JAR /META-INF/MANIFEST.MF file.
     *
     * @return the current module specification version
     */
    private String fetchCurrentModuleSpecificationVersion() {
        String version = "unknown";
        try {
            final Class<?> clazz = getClass();
            final URL classPathURL = clazz.getResource(clazz.getSimpleName() + ".class");
            if (classPathURL != null) {
                final String classPath = classPathURL.toString();
                String manifestPath;
                if (classPath.startsWith("jar")) {
                    manifestPath = classPath.substring(0, classPath.lastIndexOf('!') + 1) + "/META-INF/MANIFEST.MF";
                } else {
                    // class not from jar archive
                    final String relativePath = clazz.getName().replace('.', File.separatorChar) + ".class";
                    final String classFolder = classPath.substring(0, classPath.length() - relativePath.length() - 1);
                    manifestPath = classFolder + "/META-INF/MANIFEST.MF";
                }
                final Attributes attributes = new Manifest(new URL(manifestPath).openStream()).getMainAttributes();
                if (attributes.getValue("Version") != null) {
                    version = attributes.getValue("Version");
                }
            }
        } catch (Exception ignored) {
            //ignored
        }
        return version;
    }

    /**
     * Copies the GDAL distribution/JNI drivers files from distribution package to the target install directory.
     *
     * @param gdalVersion the GDAL version for which files will be installed
     * @throws IOException When IO error occurs
     */
    final void copyDistribution(GDALVersion gdalVersion) throws IOException {
        logger.fine("Copy the GDAL distribution to folder '" + this.gdalNativeLibrariesFolderPath.toString() + "'.");
        final String moduleVersion = fetchCurrentModuleSpecificationVersion();

        logger.fine("The module version is '" + moduleVersion + "'.");
        logger.fine("Check the GDAL distribution folder from the local disk.");

        boolean canCopyGDALDistribution = true;

        final Path gdalDistributionRootFolderPath = gdalVersion.getLocationPath();

        if (Files.exists(this.gdalNativeLibrariesFolderPath)) {
            // the GDAL distribution folder already exists on the local disk
            final String savedVersion = fetchSavedModuleSpecificationVersion(this.gdalNativeLibrariesFolderPath);
            logger.fine("The saved GDAL distribution folder version is '" + savedVersion + "'.");

            boolean isDistributionRootFolderEmpty = true;
            try (Stream<Path> s = Files.list(gdalDistributionRootFolderPath)) {
                isDistributionRootFolderEmpty = !s.findAny().isPresent();
            } catch (Exception ignored) {
                //nothing to do
            }

            if (!StringUtilities.isNullOrEmpty(savedVersion) && compareVersions(savedVersion, moduleVersion) >= 0 && Files.exists(gdalDistributionRootFolderPath) && !isDistributionRootFolderEmpty && Files.exists(gdalVersion.getEnvironmentVariablesFilePath()) && Files.exists(gdalVersion.getLoaderLibraryFilePath())) {
                canCopyGDALDistribution = false;
            } else {
                // different module versions and delete the library saved on the local disk
                FileUtilities.deleteTree(this.gdalNativeLibrariesFolderPath);
            }
        }

        if (canCopyGDALDistribution) {
            logger.fine("create the folder '" + this.gdalNativeLibrariesFolderPath + "' to copy the GDAL distribution.");
            Files.createDirectories(this.gdalNativeLibrariesFolderPath);
            copyEnvironmentVariablesNativeLibrary(gdalVersion);
            copyLoaderLibrary(gdalVersion);
            copyDistributionArchiveAndInstall(gdalDistributionRootFolderPath, gdalVersion);
            fixUpPermissions(this.gdalNativeLibrariesFolderPath);
            setSavedModuleSpecificationVersion(moduleVersion, this.gdalNativeLibrariesFolderPath);
        }
    }

    private static boolean wasSymlink(org.apache.commons.compress.archivers.zip.ZipFile zipFile, ZipEntry zipEntry, Path zipPath) {
        try {
            if(((ZipArchiveEntry)zipEntry).isUnixSymlink()) {
                final String fileContent = zipFile.getUnixSymlink((ZipArchiveEntry) zipEntry);
                final Path target = Paths.get(fileContent);
                final Path zipDir = zipPath.getParent();
                final Path zipEntryPath = Paths.get(zipEntry.getName());
                final Path zipEntryDir = zipEntryPath.getParent();
                final Path zipEntryAbsolutePath = zipDir.resolve(zipEntryDir);
                final String targetAbsolutePath = new File(zipEntryAbsolutePath.resolve(target).toString()).getCanonicalPath();
                final String zipDirAbsolutePath = new File(zipDir.toString()).getCanonicalPath();
                final Path linkPath = zipEntryAbsolutePath.resolve(zipEntryPath.getFileName());
                if (targetAbsolutePath.startsWith(zipDirAbsolutePath)) {
                    Files.createSymbolicLink(linkPath, target);
                    return true;
                }
            }
        } catch (Exception e) {
            //ignore
        }
        return false;
    }

    private static void installDistribution(Path sourceFile, Path destination) throws IOException {
        if (sourceFile == null || destination == null) {
            throw new IllegalArgumentException("One of the arguments is null");
        }
        if (!Files.exists(destination)) {
            Files.createDirectory(destination);
        }
        try (org.apache.commons.compress.archivers.zip.ZipFile zipFile = new org.apache.commons.compress.archivers.zip.ZipFile(sourceFile.toFile())) {
            ZipEntry entry;
            final Enumeration<? extends ZipEntry> entries = zipFile.getEntries();
            final byte[] buffer = new byte[262144];
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.isDirectory())
                    continue;
                final Path filePath = destination.resolve(entry.getName());
                if (Files.exists(filePath))
                    return;
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else if(!wasSymlink(zipFile, entry, sourceFile)) {
                    int read;
                    try (final InputStream inputStream = zipFile.getInputStream((ZipArchiveEntry) entry)) {
                        Files.createDirectories(filePath.getParent());
                        try (final BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(filePath))) {
                            while ((read = inputStream.read(buffer)) > 0) {
                                bos.write(buffer, 0, read);
                            }
                        }
                    }
                }
            }
        }
    }
}
