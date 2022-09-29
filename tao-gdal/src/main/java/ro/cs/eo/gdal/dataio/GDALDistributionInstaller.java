package ro.cs.eo.gdal.dataio;

import org.apache.commons.lang3.SystemUtils;
import org.esa.s2tbx.jni.EnvironmentVariables;
import ro.cs.eo.gdal.dataio.drivers.OSR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * GDAL Distribution Installer class for installing GDAL on SNAP (internal distribution or JNI drivers).
 *
 * @author Jean Coravu
 * @author Adrian Drăghici
 */
class GDALDistributionInstaller {
    private static final Logger logger = Logger.getLogger(GDALDistributionInstaller.class.getName());

    private GDALDistributionInstaller() {
    }

    /**
     * Installs the internal GDAL library distribution if missing from SNAP and not installed on OS.
     *
     * @param gdalVersion the GDAL version to be installed
     * @throws IOException When IO error occurs
     */
    private static void installDistribution(GDALVersion gdalVersion) throws IOException {
        // install the GDAL library from the distribution
        OSCategory osCategory = gdalVersion.getOsCategory();
        if (osCategory.getArchitecture() == null) {
            String msg = "No distribution folder found on " + osCategory.getOperatingSystemName() + ".";
            logger.warning(msg);
            throw new IllegalStateException(msg);
        }
        Path gdalNativeLibrariesRootFolderPath = gdalVersion.getNativeLibrariesRootFolderPath();

        logger.fine("Install the GDAL library from the distribution on " + osCategory.getOperatingSystemName() + ".");

        final Path gdalDistributionRootFolderPath = new GDALInstaller(gdalNativeLibrariesRootFolderPath).copyDistribution(gdalVersion);
        GDALLoader.loadEnvironmentVariablesNativeLibrary(gdalVersion);

        logger.fine("The GDAL library has been copied on the local disk.");

        if (org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS) {
            logger.fine("Process the GDAL library on Windows.");
            processInstalledWindowsDistribution(gdalDistributionRootFolderPath);
        } else if (org.apache.commons.lang.SystemUtils.IS_OS_LINUX || org.apache.commons.lang.SystemUtils.IS_OS_MAC_OSX) {
            String currentFolderPath = EnvironmentVariables.getCurrentDirectory();
            GDALInstaller.fixUpPermissions(gdalDistributionRootFolderPath);
            try {
                logger.fine("Process the GDAL library on Linux. The current folder is '" + currentFolderPath + "'.");
                processInstalledLinuxDistribution(gdalDistributionRootFolderPath);
            } finally {
                EnvironmentVariables.changeCurrentDirectory(currentFolderPath);
            }
        }
        logger.fine("The GDAL library has been successfully installed.");
    }

    /**
     * Installs the GDAL JNI drivers if missing from SNAP and GDAL distribution installed on OS.
     *
     * @param gdalVersion the GDAL version to which JNI drivers be installed
     * @throws IOException When IO error occurs
     */
    private static void installJNI(GDALVersion gdalVersion) throws IOException {
        // install the GDAL JNI drivers from the distribution
        OSCategory osCategory = gdalVersion.getOsCategory();
        if (osCategory.getArchitecture() == null) {
            String msg = "No distribution folder found on " + osCategory.getOperatingSystemName() + ".";
            logger.warning(msg);
            throw new IllegalStateException(msg);
        }
        Path gdalNativeLibrariesRootFolderPath = gdalVersion.getNativeLibrariesRootFolderPath();

        logger.fine("Install the GDAL JNI drivers from the distribution on " + osCategory.getOperatingSystemName() + ".");

        Path gdalDistributionRootFolderPath = new GDALInstaller(gdalNativeLibrariesRootFolderPath).copyDistribution(gdalVersion);

        logger.fine("The GDAL JNI drivers has been copied on the local disk.");
        logger.fine("Process the GDAL JNI drivers on " + gdalVersion.getOsCategory().getOperatingSystemName() + ".");
        logger.fine("Register native lib paths on " + gdalVersion.getOsCategory().getOperatingSystemName() + " for folder '" + gdalDistributionRootFolderPath.toString() + "'.");
        logger.fine("The GDAL library has been successfully installed.");
    }

    /**
     * Processes the UNIX OS specific post-install steps.
     * - adds the absolute path of the internal GDAL distribution installation location to the 'java.library.path'
     * - updates the PATH environment variable with the absolute path of the internal GDAL distribution installation location, when needed
     * - adds GDAL_DATA, GDAL_PLUGINS and PROJ_LIB environment variables
     *
     * @param gdalDistributionRootFolderPath the absolute path to the internal GDAL distribution installation location
     */
    private static void processInstalledLinuxDistribution(Path gdalDistributionRootFolderPath) {
        Path libFolderPath = gdalDistributionRootFolderPath.resolve("lib");

        String pathEnvironment = EnvironmentVariables.getEnvironmentVariable("PATH");
        final StringBuilder builder = new StringBuilder();
        builder.append("PATH=").append(libFolderPath).append(File.pathSeparator).append(pathEnvironment);
        logger.fine("Set the PATH environment variable on Linux with '" + builder + "'.");
        EnvironmentVariables.setEnvironmentVariable(builder.toString());

        Path gdalDataFolderPath = gdalDistributionRootFolderPath.resolve("share/gdal");
        builder.setLength(0);
        builder.append("GDAL_DATA=").append(gdalDataFolderPath);
        logger.fine("Set the GDAL_DATA environment variable on Linux with '" + builder + "'.");

        EnvironmentVariables.setEnvironmentVariable(builder.toString());
        Path gdalPluginsFolderPath = libFolderPath.resolve("gdalplugins");
        builder.setLength(0);
        builder.append("GDAL_PLUGINS=").append(gdalPluginsFolderPath);
        logger.fine("Set the GDAL_PLUGINS environment variable on Linux with '" + builder + "'.");

        EnvironmentVariables.setEnvironmentVariable(builder.toString());
        Path projDataFolderPath = gdalDistributionRootFolderPath.resolve("share/proj");
        builder.setLength(0);
        builder.append("PROJ_LIB=").append(projDataFolderPath);
        logger.fine("Set the PROJ_LIB environment variable on MacOSX with '" + builder + "'.");

        EnvironmentVariables.setEnvironmentVariable(builder.toString());
        String gdalPamValue = "GDAL_PAM_ENABLED=NO";
        logger.fine("Set the GDAL_PAM_ENABLED environment variable on Linux with 'NO'.");

        EnvironmentVariables.setEnvironmentVariable(gdalPamValue);//disable the creation of aux.xml file
    }

    /**
     * Processes the Windows OS specific post-install steps.
     * - adds the absolute path of the internal GDAL distribution installation location to the 'java.library.path'
     * - updates the PATH environment variable with the absolute path of the internal GDAL distribution installation location, when needed
     * - adds GDAL_DATA, GDAL_PLUGINS and PROJ_LIB environment variables
     *
     * @param gdalDistributionRootFolderPath the absolute path to the internal GDAL distribution installation location
     * @throws IOException When IO error occurs
     */
    private static void processInstalledWindowsDistribution(Path gdalDistributionRootFolderPath) throws IOException {

        String pathEnvironment = EnvironmentVariables.getEnvironmentVariable("PATH");
        final StringBuilder builder = new StringBuilder();
        builder.append("PATH=").append(gdalDistributionRootFolderPath).append(File.pathSeparator).append(pathEnvironment);
        logger.fine("Set the PATH environment variable on Windows with '" + builder + "'.");
        EnvironmentVariables.setEnvironmentVariable(builder.toString());

        Path gdalDataFolderPath = gdalDistributionRootFolderPath.resolve("gdal-data");
        builder.setLength(0);
        builder.append("GDAL_DATA=").append(gdalDataFolderPath);
        logger.fine("Set the GDAL_DATA environment variable on Windows with '" + builder + "'.");
        EnvironmentVariables.setEnvironmentVariable(builder.toString());

        Path gdalPluginsFolderPath = gdalDistributionRootFolderPath.resolve("gdalplugins");
        builder.setLength(0);
        builder.append("GDAL_PLUGINS=").append(gdalPluginsFolderPath);
        logger.fine("Set the GDAL_PLUGINS environment variable on Windows with '" + builder + "'.");
        EnvironmentVariables.setEnvironmentVariable(builder.toString());

        String gdalPamValue = "GDAL_PAM_ENABLED=NO";
        logger.fine("Set the GDAL_PAM_ENABLED environment variable on Windows with 'NO'.");
        EnvironmentVariables.setEnvironmentVariable(gdalPamValue);//disable the creation of aux.xml file
    }

    /**
     * Checks whether a directory path exists on the environment variable value.
     *
     * @param folderPathToCheck the directory path to be checked
     * @param pathEnvironment  the environment variable value
     * @return {@code true} if directory path found on environment variable value
     * @throws IOException When IO error occurs
     */
    private static boolean findFolderInPathEnvironment(Path folderPathToCheck, String pathEnvironment) throws IOException {
        String fullFolderPath = folderPathToCheck.toFile().getCanonicalPath();
        boolean foundFolderInPath = false;
        StringTokenizer str = new StringTokenizer(pathEnvironment, File.pathSeparator);
        while (str.hasMoreTokens() && !foundFolderInPath) {
            String currentFolderPathAsString = str.nextToken();
            Path currentFolderPath = Paths.get(currentFolderPathAsString);
            String currentFullFolderPath = currentFolderPath.toFile().getCanonicalPath();
            if (currentFullFolderPath.equals(fullFolderPath)) {
                foundFolderInPath = true;
            }
        }
        return foundFolderInPath;
    }

    /**
     * Setups the GDAL distribution for SNAP
     *
     * @param gdalVersion the GDAL version to be setup
     * @throws IOException When IO error occurs
     */
    static void setupDistribution(GDALVersion gdalVersion) throws IOException {
        if (gdalVersion.isJni()) {
            installJNI(gdalVersion);
        } else {
            installDistribution(gdalVersion);
        }
    }

    /**
     * Setups the GDAL Proj Lib path to correctly load the proj.db from internal distribution
     *
     * @param gdalVersion the GDAL version to be setup
     */
    static void setupProj(GDALVersion gdalVersion){
        if (!gdalVersion.isJni()) {
            final Path projPath = SystemUtils.IS_OS_LINUX
                    ? gdalVersion.getLocationPath().resolve("share").resolve("proj")
                    : gdalVersion.getLocationPath().resolve("projlib");
            OSR.setPROJSearchPath(projPath.toString());
            logger.fine("GDAL projection folder set to " + projPath);
        }
    }
}
