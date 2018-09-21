/*
 * Copyright (C) 2017 CS ROMANIA
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

package ro.cs.tao.utils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtilities {

    private static final Logger logger = Logger.getLogger(FileUtilities.class.getName());

    public static String getExtension(Path path) {
        return path != null ? getExtension(path.toString()) : null;
    }

    public static String getExtension(File file) {
        return file != null ? getExtension(file.toString()) : null;
    }

    public static String getExtension(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        String fileName = Paths.get(path).getFileName().toString();
        int idx = fileName.lastIndexOf('.');
        return idx > 0 ? fileName.substring(idx) : null;
    }

    public static String getFilenameWithoutExtension(Path path) {
        return path != null ? getFilenameWithoutExtension(path.toString()) : null;
    }

    public static String getFilenameWithoutExtension(File file) {
        return file != null ? getFilenameWithoutExtension(file.toString()) : null;
    }

    public static String getFilenameWithoutExtension(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        String fileName = Paths.get(path).getFileName().toString();
        int idx = fileName.lastIndexOf('.');
        return idx > 0 ? fileName.substring(0, idx) : fileName;
    }

    public static Path unzip(Path zipFile) throws IOException {
        URI uri = URI.create("jar:file:" + zipFile.toUri().getPath());
        Map<String, String> env = new HashMap<>();
        env.put("create", "false");
        Path destination = zipFile.getParent().resolve(getFilenameWithoutExtension(zipFile.toFile()));
        if (Files.notExists(destination)) {
            Files.createDirectories(destination);
        }
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(uri, env)) {
            final Path root = zipFileSystem.getPath("/");
            Files.walkFileTree(root, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs) throws IOException {
                    final Path destFile = Paths.get(destination.toString(),
                                                    file.toString());
                    Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                                                         BasicFileAttributes attrs) throws IOException {
                    final Path dirToCreate = Paths.get(destination.toString(),
                                                       dir.toString());
                    if(Files.notExists(dirToCreate)){
                        Files.createDirectory(dirToCreate);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        return destination;
    }

    public static Path ensureExists(Path folder) throws IOException {
        if (folder != null && !Files.exists(folder)) {
            if (isPosixFileSystem()) {
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-xr-x");
                FileAttribute<Set<PosixFilePermission>> attrs = PosixFilePermissions.asFileAttribute(perms);
                folder = Files.createDirectories(folder, attrs);
            } else {
                folder = Files.createDirectories(folder);
            }

        }
        return folder;
    }

    public static Path ensurePermissions(Path file) {
        try {
            if (file != null && Files.exists(file)) {
                if (isPosixFileSystem()) {
                    Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-xr-x");
                    file = Files.setPosixFilePermissions(file, perms);
                }
            }
        } catch (IOException ex) {
            logger.warning(String.format("Cannot set permissions for %s", file.toString()));
        }
        return file;
    }

    public static List<Path> listFolders(Path root) throws IOException {
        final List<Path> folders = new ArrayList<>();
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                folders.add(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        return folders;
    }

    public static List<Path> listFiles(Path folder) {
        final File[] files = folder.toFile().listFiles();
        return files != null ?
                Arrays.stream(files)
                        .filter(File::isFile)
                        .map(File::toPath)
                        .collect(Collectors.toList()) :
                new ArrayList<>();
    }

    public static List<Path> listFilesWithExtension(Path folder, String extension) {
        final File[] files = folder.toFile().listFiles();
        return files != null ?
                Arrays.stream(files)
                        .filter(f -> f.isFile() && f.getName().endsWith(extension))
                        .map(File::toPath)
                        .collect(Collectors.toList()) :
                new ArrayList<>();
    }

    public static Path link(Path sourcePath, final Path targetPath) throws IOException {
        return link(sourcePath, targetPath, null, null);
    }

    public static Path link(Path sourcePath, final Path targetPath,
                            Predicate<? super Path> folderFilter,
                            Predicate<? super Path> fileFilter) throws IOException {
        if (sourcePath != null && Files.exists(sourcePath)) {
            if (Files.isDirectory(sourcePath)) {
                List<Path> folders = FileUtilities.listFolders(sourcePath);
                if (folderFilter != null) {
                    folders = folders.stream().filter(folderFilter).collect(Collectors.toList());
                }
                for (Path folder : folders) {
                    FileUtilities.ensureExists(targetPath.resolve(sourcePath.relativize(folder)));
                    List<Path> files = FileUtilities.listFiles(folder);
                    if (fileFilter != null) {
                        files = files.stream().filter(fileFilter).collect(Collectors.toList());
                    }
                    for (Path file : files) {
                        linkFile(file, targetPath.resolve(sourcePath.relativize(file)));
                    }
                }
            } else {
                linkFile(sourcePath, targetPath);
            }
        }
        return targetPath;
    }

    public static Path linkFile(Path sourcePath, Path file) throws IOException {
        return Files.exists(file) ? file : Files.createSymbolicLink(file, sourcePath);
    }

    public static void deleteTree(Path root) throws IOException {
        if (root != null && Files.isDirectory(root)) {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw exc;
                    }
                }
            });
        }
    }

    public static long folderSize(Path folder) throws IOException {
        return Files.walk(folder)
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();
    }

    public static void copyFile(URL sourceURL, Path destinationFile) throws IOException {
        if (sourceURL == null || destinationFile == null) {
            throw new IllegalArgumentException("One of the arguments is null");
        }
        InputStream inputStream = sourceURL.openStream();
        try {
            File dest = destinationFile.toFile();
            dest.getParentFile().mkdirs();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest));
            try {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = inputStream.read(buffer)) > 0) {
                    bos.write(buffer, 0, read);
                }
            } finally {
                bos.close();
            }
        } finally {
            inputStream.close();
        }
    }

    public static void unzip(Path sourceFile, Path destination, boolean keepFolderStructure) throws IOException {
        if (sourceFile == null || destination == null) {
            throw new IllegalArgumentException("One of the arguments is null");
        }
        if (!Files.exists(destination)) {
            Files.createDirectory(destination);
        }
        byte[] buffer;
        try (ZipFile zipFile = new ZipFile(sourceFile.toFile())) {
            ZipEntry entry;
            long size = 0;
            long totalRead = 0;
            int progress = 0;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                size += (entry.getSize() > 0 ? entry.getSize() : 0);
            }
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                if (entry.isDirectory() && !keepFolderStructure)
                    continue;
                Path filePath = destination.resolve(entry.getName());
                Path strippedFilePath = destination.resolve(filePath.getFileName());
                if (!Files.exists(filePath)) {
                    if (entry.isDirectory()) {
                        Files.createDirectories(filePath);
                    } else {
                        try (InputStream inputStream = zipFile.getInputStream(entry)) {
                            try (BufferedOutputStream bos = new BufferedOutputStream(
                                    new FileOutputStream(keepFolderStructure ? filePath.toFile() : strippedFilePath.toFile()))) {
                                buffer = new byte[4096];
                                int read;
                                while ((read = inputStream.read(buffer)) > 0) {
                                    bos.write(buffer, 0, read);
                                    totalRead += read;
                                    progress = (int) (totalRead / size * 100);
                                    if (progress % 10 == 0) {
                                        logger.finest(String.format("Unzip %s: %s%%", sourceFile, progress));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static Boolean supportsPosix;

    private static boolean isPosixFileSystem() {
        if (supportsPosix == null) {
            supportsPosix = Boolean.FALSE;
            FileSystem fileSystem = FileSystems.getDefault();
            Iterable<FileStore> fileStores = fileSystem.getFileStores();
            for (FileStore fs : fileStores) {
                supportsPosix = fs.supportsFileAttributeView(PosixFileAttributeView.class);
                if (supportsPosix) {
                    break;
                }
            }
        }
        return supportsPosix;
    }
}
