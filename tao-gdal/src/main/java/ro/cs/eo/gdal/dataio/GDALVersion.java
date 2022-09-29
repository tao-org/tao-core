package ro.cs.eo.gdal.dataio;

import org.apache.commons.lang.SystemUtils;
import ro.cs.tao.component.SystemVariable;
import ro.cs.tao.configuration.ConfigurationManager;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * GDAL Version enum for defining compatible GDAL versions with SNAP.
 *
 * @author Adrian Drăghici
 */
public enum GDALVersion {

    GDAL_340_FULL("3.4.0", "3-4-0", new String[]{"gdalalljni"});

    private static final String VERSION_NAME = "{version}";
    private static final String JNI_NAME = "{jni}";
    private static final String DIR_NAME = "gdal-" + VERSION_NAME + JNI_NAME;
    private static final String ZIP_NAME = DIR_NAME + ".zip";
    private static final String GDAL_NATIVE_LIBRARIES_ROOT = "gdal";
    private static final String GDAL_NATIVE_LIBRARIES_SRC = "auxdata/gdal";
    private static final String GDAL_JNI_LIBRARY_FILE = "java/gdal.jar";
    private static final String GDAL_LOADER_LIBRARY_FILE = "NativeLibraryLoader.jar";

    //private static final String GDALINFIO_EXECUTABLE_NAME = "gdalinfo";
    //private static final String GDAL_INFO_CMD = GDALINFIO_EXECUTABLE_NAME + " --version";
    private static final String GDAL_AUXDATA_PATH_KEY = "gdal.auxdata.path";
    private static final String SNAP_PREFS_DIR="auxdata";

    private static final Logger logger = Logger.getLogger(GDALVersion.class.getName());

    private static final GDALVersion internalVersion = retrieveInternalVersion();
    //private static final GDALVersion installedVersion = retrieveInstalledVersion();

    final String id;
    final String name;
    String location;
    final String[] nativeLibraryNames;
    OSCategory osCategory;

    /**
     * Creates new instance for this enum.
     *
     * @param id                 the id of version
     * @param name               the name of version
     * @param nativeLibraryNames the name(s) of native library file(s)
     */
    GDALVersion(String id, String name, String[] nativeLibraryNames) {
        this.id = id;
        this.name = name;
        this.nativeLibraryNames = nativeLibraryNames;
    }

    /**
     * Gets the installed GDAL version when found or internal GDAL version otherwise.
     *
     * @return the installed GDAL version when found or internal GDAL version otherwise
     */
    public static GDALVersion getGDALVersion() {
        logger.info("Internal GDAL " + internalVersion.getId() + " will be used by TAO.");
        return internalVersion;
    }

    /**
     * Retrieves internal GDAL version from SNAP distribution packages.
     *
     * @return the internal GDAL version
     */
    private static GDALVersion retrieveInternalVersion() {
        final GDALVersion gdalVersion = GDAL_340_FULL;
        gdalVersion.setOsCategory(OSCategory.getOSCategory());
        gdalVersion.setLocation(gdalVersion.getNativeLibrariesRootFolderPath().resolve(gdalVersion.getDirName()).toString());
        return gdalVersion;
    }

    /**
     * Gets the id of this version.
     *
     * @return the id of this version
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the location of this version.
     *
     * @return the location of this version
     */
    String getLocation() {
        return this.location;
    }

    /**
     * Sets the location of this version.
     *
     * @param location the new location
     */
    private void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the OS category of this version.
     *
     * @return the OS category of this version
     */
    public OSCategory getOsCategory() {
        return this.osCategory;
    }

    /**
     * Sets the OS category of this version
     *
     * @param osCategory the new OS category
     */
    private void setOsCategory(OSCategory osCategory) {
        this.osCategory = osCategory;
    }

    /**
     * Gets whether this version is JNI driver.
     *
     * @return {@code true} if this version is JNI driver
     */
    public boolean isJni() {
        return this.nativeLibraryNames == null;
    }

    /**
     * Gets the name of directory for this version.
     *
     * @return the name of directory for this version
     */
    private String getDirName() {
        if (isJni()) {
            return DIR_NAME.replace(VERSION_NAME, this.name).replace(JNI_NAME, "-jni");
        } else {
            return DIR_NAME.replace(VERSION_NAME, this.name).replace(JNI_NAME, "");
        }
    }

    /**
     * Gets the name of ZIP archive for this version.
     *
     * @return the name of ZIP archive for this version
     */
    private String getZipName() {
        if (isJni()) {
            return ZIP_NAME.replace(VERSION_NAME, this.name).replace(JNI_NAME, "-jni");
        } else {
            return ZIP_NAME.replace(VERSION_NAME, this.name).replace(JNI_NAME, "");
        }
    }

    /**
     * Gets the relative path of the directory based on OS category for this version.
     *
     * @return the relative path of the directory based on OS category for this version
     */
    private String getDirectory() {
        return this.osCategory.getOperatingSystemName() + "/" + this.osCategory.getArchitecture();
    }

    /**
     * Gets the ZIP archive URL from SNAP distribution packages for this version.
     *
     * @return the ZIP archive URL from SNAP distribution packages for this version
     */
    public URL getZipFileURLFromSources() {
        final String zipFileDirectoryFromSources = GDAL_NATIVE_LIBRARIES_SRC + "/" + getDirectory() + "/" + getZipName();
        try {
            logger.fine("version zip archive URL from sources: '" + zipFileDirectoryFromSources + "'.");
            return getClass().getClassLoader().getResource(zipFileDirectoryFromSources.replace(File.separator, "/"));
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Gets the ZIP archive path for install this version.
     *
     * @return the ZIP archive path for install this version
     */
    public Path getZipFilePath() {
        return getLocationPath().resolve(getZipName());
    }

    /**
     * Gets the root directory path for install this version.
     *
     * @return the root directory path for install this version
     */
    public Path getLocationPath() {
        return Paths.get(getLocation());
    }

    /**
     * Gets the environment variables native library URL from SNAP distribution packages for this version.
     *
     * @return the environment variables native library URL from SNAP distribution packages for this version
     */
    public URL getEnvironmentVariablesFilePathFromSources() {
        final String evFileDirectoryFromSources = GDAL_NATIVE_LIBRARIES_SRC + "/" + getDirectory() + "/" + System.mapLibraryName(this.osCategory.getEnvironmentVariablesFileName());
        try {
            return getClass().getClassLoader().getResource(evFileDirectoryFromSources.replace(File.separator, "/"));
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Gets the loader library URL from SNAP distribution packages for this version.
     *
     * @return the loader library URL from SNAP distribution packages for this version
     */
    public URL getLoaderFilePathFromSources() {
        final String loaderFileDirectoryFromSources = GDAL_NATIVE_LIBRARIES_SRC + "/" + GDAL_LOADER_LIBRARY_FILE;
        try {
            return getClass().getClassLoader().getResource(loaderFileDirectoryFromSources.replace(File.separator, "/"));
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Gets the environment variables native library root directory path for install this version.
     *
     * @return the environment variables native library root directory path for install this version
     */
    public Path getEnvironmentVariablesFilePath() {
        return getNativeLibrariesRootFolderPath().resolve(System.mapLibraryName(this.osCategory.getEnvironmentVariablesFileName()));
    }

    /**
     * Gets the 'auxdata' directory path for the SNAP application.
     *
     * @return the root directory path for install this version
     */
    private static Path getAuxDataPath(){
        final String path = ConfigurationManager.getInstance().getValue(GDAL_AUXDATA_PATH_KEY);
        return path != null
               ? Paths.get(path).resolve(SNAP_PREFS_DIR)
               : Paths.get(SystemVariable.ROOT.value()).getParent().resolve(SNAP_PREFS_DIR);
    }

    /**
     * Gets the root directory path for install this version.
     *
     * @return the root directory path for install this version
     */
    public Path getNativeLibrariesRootFolderPath() {
        return getAuxDataPath().resolve(GDAL_NATIVE_LIBRARIES_ROOT);
    }

    /**
     * Gets the path for JNI drivers of this version.
     *
     * @return the path for JNI drivers of this version
     */
    public Path getJNILibraryFilePath() {
        return getNativeLibrariesRootFolderPath().resolve(getDirName()).resolve(GDAL_JNI_LIBRARY_FILE);
    }

    /**
     * Gets the GDAL native library files path for install this version.
     *
     * @return the GDAL native library files path for install this version
     */
    public Path[] getGDALNativeLibraryFilesPath() {
        final Path[] gdalNativeLibaryFiles = new Path[this.nativeLibraryNames.length];
        for (int i = 0; i < this.nativeLibraryNames.length; i++) {
            gdalNativeLibaryFiles[i] = getLocationPath();
            if (SystemUtils.IS_OS_LINUX) {
                gdalNativeLibaryFiles[i] = gdalNativeLibaryFiles[i].resolve("lib").resolve("jni");
            }
            gdalNativeLibaryFiles[i] = gdalNativeLibaryFiles[i].resolve(System.mapLibraryName(this.nativeLibraryNames[i]));
        }
        return gdalNativeLibaryFiles;
    }

    /**
     * Gets the path for Loader of this version.
     *
     * @return the path for Loader of this version
     */
    public Path getLoaderLibraryFilePath() {
        return getNativeLibrariesRootFolderPath().resolve(GDAL_LOADER_LIBRARY_FILE);
    }
}
