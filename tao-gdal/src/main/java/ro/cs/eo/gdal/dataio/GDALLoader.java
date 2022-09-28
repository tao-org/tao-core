package ro.cs.eo.gdal.dataio;

import org.esa.lib.gdal.activator.GDALInstallInfo;
import ro.cs.eo.gdal.dataio.drivers.GDAL;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * GDAL Loader class for loading GDAL native libraries from distribution.
 *
 * @author Jean Coravu
 * @author Adrian Drăghici
 */
public final class GDALLoader {

    private static final GDALLoader INSTANCE = new GDALLoader();
    private static final Logger logger = Logger.getLogger(GDALLoader.class.getName());

    private boolean gdalIsInitialized = false;
    private boolean gdalInitialisationExecuted = false;
    private GDALVersion gdalVersion;
    private URLClassLoader gdalVersionLoader;

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
                this.gdalVersionLoader = new URLClassLoader(new URL[]{this.gdalVersion.getJNILibraryFilePath().toUri().toURL()}, GDALLoader.class.getClassLoader());
                this.gdalIsInitialized = true;
                initDrivers();
                GDALDistributionInstaller.setupProj(gdalVersion);
                Path gdalDistributionBinFolderPath = Paths.get(this.gdalVersion.getLocation());
                GDALInstallInfo.INSTANCE.setLocations(gdalDistributionBinFolderPath);
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
     * Init the drivers if the GDAL library is installed.
     */
    private void initDrivers() {
        logger.fine("Init the GDAL drivers on " + this.gdalVersion.getOsCategory().getOperatingSystemName() + ".");
        GDAL.allRegister();// GDAL init drivers
    }
}
