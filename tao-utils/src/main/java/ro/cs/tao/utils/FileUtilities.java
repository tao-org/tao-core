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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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
        FileSystem zipFileSystem;
        try {
            zipFileSystem = FileSystems.newFileSystem(uri, env);
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
            zipFileSystem = FileSystems.getFileSystem(uri);
        }
        try {
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
        } finally {
            if (zipFileSystem != null) {
                zipFileSystem.close();
            }
        }
        return destination;
    }

    public static Path ensureExists(Path folder) throws IOException {
        if (folder != null && !Files.exists(folder)) {
            if (isPosixFileSystem()) {
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-xr-x");
                FileAttribute<Set<PosixFilePermission>> attrs = PosixFilePermissions.asFileAttribute(perms);
                try {
                    folder = Files.createDirectories(folder, attrs);
                } catch (Exception e) {
                    // Maybe just attributes cannot be set
                    folder = Files.createDirectories(folder);
                }
            } else {
                folder = Files.createDirectories(folder);
            }

        }
        return folder;
    }

    public static void ensureExists(Path folder, ChannelSftp clientChannel) throws IOException {
        final int nameCount = folder.getNameCount();
        Path current = folder.getRoot().resolve(folder.getName(0));
        for (int i = 1; i < nameCount; i++) {
            current = current.resolve(folder.getName(i));
            remoteCreateDirectory(current, clientChannel);
        }
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
                if (root.compareTo(dir) != 0) {
                    folders.add(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return folders;
    }

    public static List<Path> listFolders(Path root, int depth) throws IOException {
        final List<Path> folders = new ArrayList<>();
        Files.walkFileTree(root, new HashSet<FileVisitOption>() {{ add(FileVisitOption.FOLLOW_LINKS); }}, depth, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (root.compareTo(dir) != 0) {
                    folders.add(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return folders;
    }

    public static List<Path> listFiles(Path folder) {
        try {
            return Files.walk(folder, 1).collect(Collectors.toList());
        } catch (IOException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<Path> listFiles(Path folder, String pattern, LocalDateTime before) throws IOException {
        final Instant instant = before.atZone(ZoneOffset.systemDefault()).toInstant();
        final Pattern p = Pattern.compile(pattern);
        return Files.walk(folder, 1).filter(f -> {
            try {
                return p.matcher(f.getFileName().toString()).find() && Files.getLastModifiedTime(f).toInstant().isBefore(instant);
            } catch (IOException inner) {
                return false;
            }
        }).collect(Collectors.toList());
    }

    public static List<Path> listTree(Path folder) throws IOException {
        return Files.walk(folder).collect(Collectors.toList());
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
                List<Path> fileList = FileUtilities.listFiles(sourcePath).stream().filter(Files::isRegularFile).collect(Collectors.toList());
                if (fileFilter != null) {
                    fileList = fileList.stream().filter(fileFilter).collect(Collectors.toList());
                }
                for (Path file : fileList) {
                    linkFile(file, targetPath.resolve(sourcePath.relativize(file)));
                }
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

    public static boolean isPathAccessible(Path path) {
        boolean retVal;
        if (path != null) {
            try {
                retVal = ((Files.isDirectory(path) || Files.isRegularFile(path)) && Files.exists(path) && Files.isReadable(path)) ||
                        (Files.isSymbolicLink(path) && Files.isReadable(Files.readSymbolicLink(path)));
            } catch (IOException ex) {
                retVal = false;
            }
        } else {
            retVal = false;
        }
        return retVal;
    }

    public static boolean isPathWriteable(Path path) {
        boolean retVal;
        if (path != null) {
            try {
                retVal = ((Files.isDirectory(path) || Files.isRegularFile(path)) && Files.exists(path) && Files.isWritable(path)) ||
                        (Files.isSymbolicLink(path) && Files.isWritable(Files.readSymbolicLink(path)));
            } catch (IOException ex) {
                retVal = false;
            }
        } else {
            retVal = false;
        }
        return retVal;
    }

    public static Path resolveSymLinks(Path path) throws IOException {
        Path realPath = path.getRoot();
        final int nameCount = path.getNameCount();
        for (int i = 0; i < nameCount; i++) {
            realPath = realPath.resolve(path.getName(i));
            if (Files.isSymbolicLink(realPath)) {
                realPath = Files.readSymbolicLink(realPath);
            } else {
                realPath = realPath.toAbsolutePath();
            }
        }
        return realPath;
    }

    public static Path linkFile(Path sourcePath, Path file) throws IOException {
        return Files.exists(file) ? file : Files.createSymbolicLink(file, sourcePath);
    }

    public static Path linkFolder(Path sourcePath, Path target) throws IOException {
        if (!Files.isDirectory(sourcePath)) {
            throw new IOException(sourcePath + " is not a directory");
        }
        if (!Files.isDirectory(target)) {
            throw new IOException(target + " is not a directory");
        }
        return linkFile(sourcePath, target);
    }

    public static int copy(Path source, Path destination) throws IOException {
        final AtomicInteger copied = new AtomicInteger(0);
        if (source != null && destination != null) {
            if (Files.isRegularFile(source)) {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Path targetFolder = destination.resolve(source.getFileName());
                Files.createDirectories(targetFolder);
                Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path target = targetFolder.resolve(source.relativize(dir));
                        Files.createDirectories(target);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path targetFile = targetFolder.resolve(source.relativize(file));
                        Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        copied.incrementAndGet();
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (exc == null) {
                            return FileVisitResult.CONTINUE;
                        } else {
                            throw exc;
                        }
                    }
                });
            }
        }
        return copied.intValue();
    }

    public static int upload(Path source, Path destination, ChannelSftp channel) throws IOException {
        final AtomicInteger copied = new AtomicInteger(0);
        if (source != null && destination != null) {
            if (Files.isRegularFile(source)) {
                //Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                try {
                    createDirectories(destination, channel);
                    channel.cd(destination.toString());
                    channel.put(Files.newInputStream(source), source.getFileName().toString(), ChannelSftp.OVERWRITE);
                } catch (SftpException e) {
                    throw new IOException(e);
                }
            } else {
                Path targetFolder = destination.resolve(source.getFileName());
                createDirectories(targetFolder, channel);
                try {
                    channel.cd(targetFolder.toString());
                } catch (SftpException e) {
                    throw new IOException(e);
                }
                Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path target = source.relativize(dir);
                        //Files.createDirectories(target);
                        try {
                            channel.mkdir(target.toString());
                            channel.cd(target.toString());
                        } catch (SftpException e) {
                            throw new IOException(e);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path targetFile = source.relativize(file);
                        try {
                            channel.put(Files.newInputStream(file), targetFile.getFileName().toString(), ChannelSftp.OVERWRITE);
                        } catch (SftpException e) {
                            throw new IOException(e);
                        }
                        copied.incrementAndGet();
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (exc == null) {
                            return FileVisitResult.CONTINUE;
                        } else {
                            throw exc;
                        }
                    }
                });
            }
        }
        return copied.intValue();
    }

    public static void move(Path source, Path destination) throws IOException {
        if (source != null && destination != null) {
            if (!Files.exists(destination)) {
                Files.createDirectories(destination);
            }
            if (Files.isRegularFile(source)) {
                Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Path targetFolder = destination.resolve(source.getFileName());
                Files.createDirectories(targetFolder);
                Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path target = targetFolder.resolve(source.relativize(dir));
                        Files.createDirectories(target);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path targetFile = targetFolder.resolve(source.relativize(file));
                        Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
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
                //Files.delete(source);
            }
        }
    }

    public static void copyAndDelete(Path source, Path destination) throws IOException {
        if (source != null && destination != null) {
            if (!Files.exists(destination)) {
                Files.createDirectories(destination);
            }
            if (Files.isRegularFile(source)) {
                Files.deleteIfExists(destination);
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                Files.delete(source);
            } else {
                Path targetFolder = destination.resolve(source.getFileName());
                Files.createDirectories(targetFolder);
                Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path target = targetFolder.resolve(source.relativize(dir));
                        Files.createDirectories(target);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path targetFile = targetFolder.resolve(source.relativize(file));
                        Files.deleteIfExists(targetFile);
                        Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
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
                //Files.delete(source);
            }
        }
    }

    public static void remoteCopyAndDelete(Path source, Path destination, ChannelSftp channel) throws IOException {
        if (source != null && destination != null) {
            remoteCreateDirectory(destination, channel);
            if (Files.isRegularFile(source)) {
                remoteDeleteIfExists(destination, channel);
                upload(source, destination, channel);
                remoteDelete(source, channel);
            } else {
                Path targetFolder = destination.resolve(source.getFileName());
                remoteCreateDirectory(targetFolder, channel);
                Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path target = targetFolder.resolve(source.relativize(dir));
                        remoteCreateDirectory(target, channel);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path targetFile = targetFolder.resolve(source.relativize(file));
                        remoteDeleteIfExists(targetFile, channel);
                        upload(file, targetFile, channel);
                        remoteDelete(file, channel);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (exc == null) {
                            remoteDelete(dir, channel);
                            return FileVisitResult.CONTINUE;
                        } else {
                            throw exc;
                        }
                    }
                });
            }
        }
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
                    if (exc == null && !dir.equals(root)) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else if (exc != null) {
                        throw exc;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            Files.delete(root);
        }
    }

    public static void remoteDeleteTree(Path root, ChannelSftp channel) throws IOException {
        if (root != null) {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        channel.rm(file.toString());
                    } catch (SftpException e) {
                        throw new IOException(e);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc == null && !dir.equals(root)) {
                        try {
                            channel.rmdir(dir.toString());
                        } catch (SftpException e) {
                            throw new IOException(e);
                        }
                        return FileVisitResult.CONTINUE;
                    } else if (exc != null) {
                        throw exc;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            try {
                channel.rmdir(root.toString());
            } catch (SftpException e) {
                throw new IOException(e);
            }
        }
    }

    public static long folderSize(Path folder) throws IOException {
        return Files.walk(folder)
                .filter(Files::isRegularFile)
                .mapToLong(p -> p.toFile().length())
                .sum();
    }

    public static void copyFile(URL sourceURL, Path destinationFile) throws IOException {
        if (sourceURL == null || destinationFile == null) {
            throw new IllegalArgumentException("One of the arguments is null");
        }
        try (InputStream inputStream = sourceURL.openStream()) {
            File dest = destinationFile.toFile();
            dest.getParentFile().mkdirs();
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest))) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = inputStream.read(buffer)) > 0) {
                    bos.write(buffer, 0, read);
                }
            }
        }
    }

    public static void unzip(Path sourceFile, Path destination, boolean keepFolderStructure) throws IOException {
        if (sourceFile == null || destination == null) {
            throw new IllegalArgumentException("One of the arguments is null");
        }
        if (!Files.exists(destination)) {
            Files.createDirectory(destination);
        }
        try (ZipFile zipFile = new ZipFile(sourceFile.toFile())) {
            ZipEntry entry;
            long size = 0;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                size += (entry.getSize() > 0 ? entry.getSize() : 0);
            }
            logger.fine(String.format("Estimated size for %s: %.2fMB", sourceFile, (double) (size / 1048576)));
            entries = zipFile.entries();
            final byte[] buffer = new byte[262144];
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
                        int read;
                        try (InputStream inputStream = zipFile.getInputStream(entry)) {
                            Files.createDirectories(keepFolderStructure ? filePath.getParent() : strippedFilePath.getParent());
                            try (BufferedOutputStream bos = new BufferedOutputStream(
                                    new FileOutputStream(keepFolderStructure ? filePath.toFile() : strippedFilePath.toFile()))) {
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

    public static void remoteCreateDirectory(Path path, ChannelSftp channel) throws IOException {
        try {
            channel.cd(path.getParent().toString());
            channel.ls(path.getFileName().toString());
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                try {
                    channel.mkdir(path.getFileName().toString());
                } catch (SftpException sftpException) {
                    throw new IOException(sftpException);
                }
            } else {
                throw new IOException(e);
            }
        }
    }

    public static boolean remoteExists(Path path, ChannelSftp channel) throws IOException {
        try {
            channel.cd(path.getParent().toString());
            channel.ls(path.getFileName().toString());
            return true;
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return false;
            } else {
                throw new IOException(e);
            }
        }
    }

    public static void createDirectories(Path path, ChannelSftp channel) throws IOException {
        Path current = path.getRoot().resolve(path.getName(0));
        for (int i = 1; i < path.getNameCount(); i++) {
            current = current.resolve(path.getName(i));
            try {
                channel.ls(current.toString());
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    try {
                        channel.mkdir(current.getFileName().toString());
                    } catch (SftpException sftpException) {
                        throw new IOException(sftpException);
                    }
                } else {
                    throw new IOException(e);
                }
            }
        }
    }

    public static long size(Path file, ChannelSftp channel) throws IOException {
        try {
            channel.cd(file.getParent().toString());
            SftpATTRS attrs = channel.lstat(file.getFileName().toString());
            return attrs != null ? attrs.getSize() : -1;
        } catch (SftpException e) {
            throw new IOException(e);
        }
    }

    public static void remoteCopy(Path file, ChannelSftp channel) throws IOException {
        try {
            channel.cd(file.getParent().toString());
            channel.put(Files.newInputStream(file), file.getFileName().toString(), ChannelSftp.OVERWRITE);
        } catch (SftpException e) {
            throw new IOException(e);
        }
    }

    public static void remoteMove(Path source, Path destination, ChannelSftp channel) throws IOException {
        try {
            channel.cd(source.getParent().toString());
            channel.rename(source.getFileName().toString(), source.getParent().relativize(destination).toString());
        } catch (SftpException e) {
            throw new IOException(e);
        }
    }

    public static boolean remoteDeleteIfExists(Path file, ChannelSftp channel) throws IOException {
        try {
            channel.cd(file.getParent().toString());
            final String fileName = file.getFileName().toString();
            final SftpATTRS attrs = channel.lstat(fileName);
            if (attrs.isDir()) {
                channel.rmdir(fileName);
            } else {
                channel.rm(fileName);
            }
            return true;
        } catch (SftpException e) {
            if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                throw new IOException(e);
            } else {
                return false;
            }
        }
    }

    public static void remoteDelete(Path file, ChannelSftp channel) throws IOException {
        try {
            channel.cd(file.getParent().toString());
            final String fileName = file.getFileName().toString();
            final SftpATTRS attrs = channel.lstat(fileName);
            if (attrs.isDir()) {
                channel.rmdir(fileName);
            } else {
                channel.rm(fileName);
            }
        } catch (SftpException e) {
            throw new IOException(e);
        }
    }
}
