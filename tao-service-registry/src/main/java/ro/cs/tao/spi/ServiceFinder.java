/*
 * Copyright (C) 2018 CS ROMANIA
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
package ro.cs.tao.spi;

import ro.cs.tao.utils.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * A finder for service provider interface (SPI) registries.
 *
 * @author Norman Fomferra
 * @since SNAP 2.0
 */
public class ServiceFinder {

    private final String servicesPath;
    private final List<Path> searchPaths;
    private boolean useClassPath;
    private boolean useZipFiles;

    /**
     * Constructor.
     *
     * @param serviceType The service type or service provider interface (SPI).
     */
    public ServiceFinder(Class serviceType) {
        this(serviceType.getName());
    }

    /**
     * Constructor.
     *
     * @param serviceName The service's name.
     */
    public ServiceFinder(String serviceName) {
        servicesPath = "META-INF/services/" + serviceName;
        searchPaths = new ArrayList<>();
    }

    /**
     * @return The service's path.
     */
    public String getServicesPath() {
        return servicesPath;
    }

    /**
     * @return {@code true} if this ServiceFinder uses the JVM's class path�to find modules.
     */
    public boolean getUseClassPath() {
        return useClassPath;
    }

    /**
     * @param value If {@code true}, the JVM's class path shall be searched as well
     */
    public void setUseClassPath(boolean value) {
        this.useClassPath = value;
    }

    /**
     * @return {@code true} if this ServiceFinder uses the JVM's class path to find modules.
     */
    public boolean getUseZipFiles() {
        return useZipFiles;
    }

    /**
     * @param value If {@code true}, any ZIP and JAR files are searched as well
     */
    public void setUseZipFiles(boolean value) {
        this.useZipFiles = value;
    }

    /**
     * @return The list of search paths.
     */
    public List<Path> getSearchPaths() {
        return Collections.unmodifiableList(searchPaths);
    }

    /**
     * Adds a search (directory) path.
     *
     * @param path A search path.
     */
    public void addSearchPath(Path path) {
        searchPaths.add(path);
    }

    /**
     * Adds search (directory) paths.
     *
     * @param paths The search paths.
     */
    public void addSearchPaths(Path... paths) {
        searchPaths.addAll(Arrays.asList(paths));
    }

    /**
     * Finds services based on the current search path configuration.
     *
     * @return List of modules providing the services.
     */
    public List<Module> findServices() {
        List<Module> modules = new ArrayList<>();
        for (Path directory : searchPaths) {
            scanPath(directory, modules);
        }
        if (useClassPath) {
            scanClassPath(modules);
        }
        return modules;
    }

    private void addSearchPathsFromPreferencesValue(String extraPaths) {
        if (extraPaths != null) {
            addSearchPaths(Stream.of(extraPaths.split(File.pathSeparator))
                                   .map(s -> Paths.get(s))
                                   .toArray(Path[]::new));
        }
    }

    private void scanPath(Path path, List<Module> modules) {
        if (Files.isDirectory(path)) {
            scanDirectory(path, modules);
        } else {
            //LOG.warning("Can't search for SPIs, not a directory: " + path);
        	//TODO: Replace with the logger
        }
    }

    private void scanDirectory(Path directory, List<Module> modules) {
        //LOG.fine("Searching for SPIs " + servicesPath + " in " + directory);
        //TODO: Replace with the logger
        try(Stream<Path> stream = Files.list(directory)) {
            stream.forEach(entry -> {
                if (Files.isDirectory(entry)) {
                    parseServiceRegistry(entry.resolve(servicesPath), modules);
                } else if (useZipFiles) {
                    String extension = FileUtilities.getExtension(entry.toString());
                    if (".jar".compareToIgnoreCase(extension) == 0 || ".zip".compareToIgnoreCase(extension) == 0) {
                        try {
                            try (FileSystem fs = FileSystems.newFileSystem(entry, (ClassLoader) null)) {
                                parseServiceRegistry(fs.getPath(servicesPath), modules);
                            }
                        } catch (IOException e) {
                            //LOG.log(Level.SEVERE, "Failed to open file : " + entry, e);
                            //TODO: Replace with the logger
                        }
                    }
                }
            });
        } catch (IOException e) {
            //LOG.log(Level.SEVERE, "Failed to list directory: " + directory, e);
        	//TODO: Replace with the logger
        }
    }

    private void scanClassPath(List<Module> modules) {
        //LOG.fine("Searching for SPIs " + servicesPath + " in Java class path");
    	//TODO: Replace with the logger
        Collection<Path> resources = ResourceLocator.getResources(servicesPath);
        resources.forEach(path -> parseServiceRegistry(path, modules));
    }

    private void parseServiceRegistry(Path registryPath, List<Module> modules) {
        if (!Files.exists(registryPath) || !registryPath.endsWith(this.servicesPath)) {
            return;
        }

        Path moduleRoot = subtract(registryPath, Paths.get(this.servicesPath).getNameCount());

        ArrayList<String> services = new ArrayList<>();
        try {
            Files.lines(registryPath).forEach(line -> {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    services.add(line);
                }
            });
        } catch (IOException e) {
            //LOG.log(Level.SEVERE, "Failed to parse service registry file " + registryPath, e);
        	//TODO: Replace with the logger
        }

        if (!services.isEmpty()) {
            modules.add(new Module(moduleRoot, services));
        }
    }

    private static Path subtract(Path resourcePath, int nameCount) {
        Path moduleRoot = resourcePath;
        for (int i = 0; i < nameCount; i++) {
            moduleRoot = moduleRoot.resolve("..");
        }
        moduleRoot = moduleRoot.normalize();
        return moduleRoot;
    }

    @Deprecated
    public void searchClassPath(boolean value) {
        this.useClassPath = value;
    }

    /**
     * The module containing the services.
     */
    public static class Module {
        private final Path path;
        private final List<String> serviceNames;

        private Module(Path path, List<String> serviceNames) {
            this.path = path;
            this.serviceNames = serviceNames;
        }

        /**
         * The module's path.
         */
        public Path getPath() {
            return path;
        }

        /**
         * The service names parsed from the module's service registry file.
         */
        public List<String> getServiceNames() {
            return Collections.unmodifiableList(serviceNames);
        }
    }

}
