package ro.cs.eo.gdal.dataio;

import ro.cs.tao.configuration.ConfigurationManager;

import java.util.logging.Logger;

/**
 * GDAL Loader Config class for setting which GDAL version is used by SNAP (internal distribution or installed distribution).
 *
 * @author Adrian Drăghici
 */
public class GDALLoaderConfig {

    private static final String INSTANCE_CONFIG = "s2tbx";
    private static final String PREFERENCE_KEY_USE_INSTALLED_GDAL = "s2tbx.dataio.gdal.installed";

    private static final Logger logger = Logger.getLogger(GDALLoaderConfig.class.getName());
    private static final GDALLoaderConfig instance = new GDALLoaderConfig();

    private boolean useInstalledGDALLibrary;

    private GDALLoaderConfig() {
        this.useInstalledGDALLibrary = getUseInstalledGDALLibrary();
    }

    /**
     * Returns instance of this class.
     *
     * @return the instance of this class.
     */
    public static GDALLoaderConfig getInstance() {
        return instance;
    }

    /**
     * Fetches the setting for using installed distribution, from SNAP config files.
     *
     * @return the setting for using installed distribution
     */
    private boolean getUseInstalledGDALLibrary() {
        return Boolean.parseBoolean(ConfigurationManager.getInstance().getValue(PREFERENCE_KEY_USE_INSTALLED_GDAL, "false"));
    }

    /**
     * Sets the setting for using installed distribution, to SNAP config files.
     *
     * @param useInstalledGDALLibrary the setting for using installed distribution
     */
    public void setUseInstalledGDALLibrary(boolean useInstalledGDALLibrary) {
        this.useInstalledGDALLibrary = useInstalledGDALLibrary;
        ConfigurationManager.getInstance().setValue(PREFERENCE_KEY_USE_INSTALLED_GDAL, String.valueOf(this.useInstalledGDALLibrary));
    }

    /**
     * Gets whether installed distribution is set to be used by SNAP.
     *
     * @return {@code true} if installed distribution is set to be used by SNAP
     */
    public boolean useInstalledGDALLibrary() {
        return this.useInstalledGDALLibrary;
    }

}
