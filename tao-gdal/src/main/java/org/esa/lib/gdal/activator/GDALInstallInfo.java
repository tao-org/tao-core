package org.esa.lib.gdal.activator;

import ro.cs.tao.configuration.ConfigurationManager;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * GDAL Install Info class which stores the location of installed GDAL distribution.
 *
 * @author Cosmin Cara
 */
public class GDALInstallInfo {
    public static final GDALInstallInfo INSTANCE = new GDALInstallInfo();

    private Path binLocation;

    private GDALInstallInfo(){
        //nothing to init
    }

    /**
     * Sets the location which contains GDAL binaries.
     *
     * @param binLocation the location which contains GDAL binaries
     */
    public synchronized void setLocations(Path binLocation) {
        this.binLocation = binLocation;
        ConfigurationManager.getInstance().setValue("gdal.apps.path", this.binLocation.toString());
    }

    /**
     * Checks whether the location which contains GDAL binaries is stored and exists
     *
     * @return {@code true} if the location which contains GDAL binaries is stored and exists
     */
    public boolean isPresent() {
        return this.binLocation != null && Files.exists(this.binLocation);
    }
}


