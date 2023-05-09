package ro.cs.eo.gdal.dataio;

import org.esa.lib.gdal.activator.GDALInstallInfo;
import ro.cs.eo.gdal.dataio.drivers.GDAL;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * GDAL Loader class for loading GDAL native libraries from distribution.
 *
 * @author Jean Coravu
 * @author Adrian Drăghici
 */
public final class GDALLoader {

    private static final GDALLoader INSTANCE = new GDALLoader();

    private static final String GDAL_NATIVE_LIBRARY_LOADER_CLASS_NAME = "ro.cs.eo.NativeLibraryLoader";
    private static final Logger logger = Logger.getLogger(GDALLoader.class.getName());

    private boolean gdalIsInitialized = false;
    private boolean gdalInitialisationExecuted = false;
    private GDALVersion gdalVersion;
    private GDALLoaderClassLoader gdalVersionLoader;

    private GDALLoader() {

    }

    /**
     * Returns instance of this class.
     *
     * @return the instance of this class.
     */
    public static GDALLoader getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes GDAL native libraries to be used by SNAP.
     */
    public void initGDAL() {
        if (!this.gdalInitialisationExecuted) {
            try {
                this.gdalVersion = GDALVersion.getGDALVersion();
                GDALDistributionInstaller.setupDistribution(this.gdalVersion);
                this.gdalVersionLoader = new GDALLoaderClassLoader(new URL[]{this.gdalVersion.getJNILibraryFilePath().toUri().toURL(), this.gdalVersion.getLoaderLibraryFilePath().toUri().toURL()}, this.gdalVersion.getGDALNativeLibraryFilesPath());
                loadGDALNativeLibrary();
                this.gdalIsInitialized = true;
                initDrivers();
                GDALDistributionInstaller.setupProj(gdalVersion);
                GDALInstallInfo.INSTANCE.setLocations(this.gdalVersion.getLocationPath());
            } catch (Throwable ex) {
                logger.severe("Failed to initialize GDAL native drivers. GDAL readers and writers were disabled."+ex.getMessage());
                if (this.gdalVersionLoader != null) {
                    try {
                        this.gdalVersionLoader.close();
                    } catch (Throwable ignored) { }
                }
                this.gdalIsInitialized = false;
            }
            this.gdalInitialisationExecuted = true;
        }
    }

    /**
     * Gets the GDAL JNI URL class loader for loading JNI drivers of current version native libraries.
     *
     * @return the GDAL JNI URL class loader for loading JNI drivers of current version native libraries
     */
    public URLClassLoader getGDALVersionLoader() {
        if (!this.gdalIsInitialized) {
            throw new IllegalStateException("GDAL Loader not initialized.");
        }
        return this.gdalVersionLoader;
    }

    /**
     * Loads the environment variables native library used for access OS environment variables.
     *
     * @param gdalVersion the GDAL version to which JNI environment variables native library be installed
     */
    static void loadEnvironmentVariablesNativeLibrary(GDALVersion gdalVersion) {
        final Path evFilePath = gdalVersion.getEnvironmentVariablesFilePath();
        logger.fine("Load the native library '" + evFilePath.getFileName() + "'.");
        System.load(evFilePath.toAbsolutePath().toString());
    }

    /**
     * Loads the GDAL native library used for access GDAL native methods.
     *
     */
    void loadGDALNativeLibrary() {
        try {
            final Method loaderMethod = this.gdalVersionLoader.loadClass(GDAL_NATIVE_LIBRARY_LOADER_CLASS_NAME).getMethod("loadNativeLibrary", Path.class);
            for (Path nativeLibraryFilePath : this.gdalVersion.getGDALNativeLibraryFilesPath()) {
                loaderMethod.invoke(null, nativeLibraryFilePath);
                Logger.getLogger(GDALLoader.class.getName()).fine("Load the GDAL native library '" + nativeLibraryFilePath.getFileName() + "'.");
            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Init the drivers if the GDAL library is installed.
     */
    private void initDrivers() {
        logger.fine("Init the GDAL drivers on " + this.gdalVersion.getOsCategory().getOperatingSystemName() + ".");
        GDAL.allRegister();// GDAL init drivers
    }
}
