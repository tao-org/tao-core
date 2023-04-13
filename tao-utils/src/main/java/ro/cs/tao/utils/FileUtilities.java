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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.utils.executors.Executor;
import ro.cs.tao.utils.executors.ExecutorType;
import ro.cs.tao.utils.executors.OutputAccumulator;
import ro.cs.tao.utils.executors.monitoring.ListenableInputStream;
import ro.cs.tao.utils.executors.monitoring.ProgressListener;

import java.io.*;
import java.net.Inet4Address;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtilities {
    private static final String[] WIN_TO_UNIX_TOKENS = new String[] {":", "\\"};
    private static final String[] WIN_TO_UNIX_REPLACEMENTS = new String[] {"", "/" };
    private static final String[] UNITS = new String[] { "B", "KB", "MB", "GB", "TB" };
    private static final Set<String> KNOWN_RASTER_EXTENSIONS = new HashSet<String>() {{
       add(".tif"); add(".TIF"); add(".tiff"); add(".TIFF");
       add(".nc"); add(".NC");
       add(".hd5"); add(".HD5");
    }};
    private static final DecimalFormat UNIT_FORMAT = new DecimalFormat("#,##0.#");
    private static final Pattern URI_PATTERN = Pattern.compile("\\w+:\\/{2,3}[A-Za-z0-9._/]+");
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

    public static boolean isRaster(Path file) {
        final String strFile = file.toString();
        return Files.isRegularFile(file) && KNOWN_RASTER_EXTENSIONS.stream().anyMatch(strFile::endsWith);
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

    public static boolean isURI(String path) {
        return URI_PATTERN.matcher(path).find();
    }

    public static Path toPath(String path) {
        if (isURI(path)) {
            return Paths.get(URI.create(path));
        } else {
            return Paths.get(path);
        }
    }

    public static String toUnixPath(String path) {
        String transformed = path;
        if (isURI(path)) {
            transformed = path.substring(path.indexOf("://") + 3);
            if (":".equals(transformed.substring(2, 3))) {
                // Windows case
                transformed = transformed.substring(transformed.indexOf(":") + 1);
            }
        } else {
            transformed = transformed.replace("\\", "/");
            int idx;
            if ((idx = transformed.indexOf(":")) > 0) {
                transformed = transformed.substring(idx + 1);
            }
        }
        return transformed;
    }

    /**
     * Uncompresses a zip file as-is. This method uses internally the ZipFileSystem class.
     * @param zipFile       The input zip file
     * @return              The path to where the zip was extracted
     */
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
            Files.walkFileTree(root, new SimpleFileVisitor<>(){
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

    /**
     * Makes sure the folders contained in the given path exist, with appropriate permissions.
     * @param folder        The folder to make sure it exists
     * @return              The same folder
     */
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
    /**
     * Makes sure the given folder exists remotely.
     * @param folder        The folder to make sure it exists
     * @param clientChannel The SFTP channel
     */
    public static void ensureExists(Path folder, ChannelSftp clientChannel) throws IOException {
        final int nameCount = folder.getNameCount();
        Path current = folder.getRoot().resolve(folder.getName(0));
        for (int i = 1; i < nameCount; i++) {
            current = current.resolve(folder.getName(i));
            remoteCreateDirectory(current, clientChannel);
        }
    }

    /**
     * Ensures that the given file has read/write permissions for the current user and read permissions for other users.
     * @param file      The input file
     */
    public static Path ensurePermissions(Path file) {
        try {
            if (file != null && Files.exists(file)) {
                if (isPosixFileSystem()) {
                    Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-xr-x");
                    file = Files.setPosixFilePermissions(file, perms);
                }
            }
        } catch (IOException ex) {
            logger.warning(String.format("Cannot set permissions for %s", file));
        }
        return file;
    }

    /**
     * Takes ownership of the given file. The caller must have the rights to call 'chown'
     * @param file  The input file
     */
    public static void takeOwnership(Path file) {
        try {
            final List<String> args = new ArrayList<>() {{
               add("chown");
               if (Files.isDirectory(file)) {
                   add("-R");
               }
               add(System.getProperty("user.name") + ":" + System.getProperty("user.name"));
               add(file.toAbsolutePath().toString());
            }};
            final Executor<?> executor = Executor.create(ExecutorType.PROCESS,
                                                         Inet4Address.getLocalHost().getHostName(),
                                                         args, true);
            final OutputAccumulator accumulator = new OutputAccumulator();
            executor.setOutputConsumer(accumulator);
            if (executor.execute(true) != 0) {
                throw new Exception(accumulator.getOutput());
            }
        } catch (Exception e) {
            logger.severe(ExceptionUtils.getStackTrace(logger, e));
        }
    }

    /**
     * Lists all the sub-folders of the given path.
     * @param root          The path to explore
     * @return              A list of folders
     */
    public static List<Path> listFolders(Path root) throws IOException {
        final List<Path> folders = new ArrayList<>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
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
    /**
     * Lists all the sub-folders of the given path, down to the given depth.
     * @param root          The path to explore
     * @param depth         The depth of the tree structure
     * @return              A list of folders
     */
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
    /**
     * Lists all the files in the given path (first-level only)
     * @param folder        The path to explore
     * @return              A list of files
     */
    public static List<Path> listFiles(Path folder) {
        try (Stream<Path> stream = Files.walk(folder, 1, FileVisitOption.FOLLOW_LINKS)) {
            return stream.collect(Collectors.toList());
        } catch (IOException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }
    /**
     * Lists all the files in the given path (first-level only) that match a pattern.
     * @param folder        The path to explore
     * @param pattern       The regex pattern
     * @return              A list of files
     */
    public static List<Path> listFiles(Path folder, String pattern) throws IOException {
        final Pattern p = Pattern.compile(pattern);
        try (Stream<Path> stream = Files.walk(folder, 1, FileVisitOption.FOLLOW_LINKS)) {
            return stream.filter(f -> p.matcher(f.getFileName().toString()).find()).collect(Collectors.toList());
        }
    }
    /**
     * Lists all the files in the given path (first-level only) that match a pattern and are older than a date/time
     * @param folder        The path to explore
     * @param pattern       The regex pattern
     * @param before        The date/time to compare with
     * @return              A list of files
     */
    public static List<Path> listFiles(Path folder, String pattern, LocalDateTime before) throws IOException {
        final Instant instant = before.atZone(ZoneOffset.systemDefault()).toInstant();
        final Pattern p = Pattern.compile(pattern);
        try (Stream<Path> stream = Files.walk(folder, 1, FileVisitOption.FOLLOW_LINKS)) {
            return stream.filter(f -> {
                try {
                    return p.matcher(f.getFileName().toString()).find() && Files.getLastModifiedTime(f).toInstant().isBefore(instant);
                } catch (IOException inner) {
                    return false;
                }
            }).collect(Collectors.toList());
        }
    }
    /**
     * Lists all the files and folders in the given path.
     * @param folder        The path to explore
     * @return              A list of files and folders
     */
    public static List<Path> listTree(Path folder) throws IOException {
        try (Stream<Path> stream = Files.walk(folder, FileVisitOption.FOLLOW_LINKS)) {
            return stream.collect(Collectors.toList());
        }
    }
    /**
     * Lists all the files in the given path (first-level only) that have a given extension.
     * @param folder        The path to explore
     * @param extension     The extension of the files
     * @return              A list of files
     */
    public static List<Path> listFilesWithExtension(Path folder, String extension) {
        final File[] files = folder.toFile().listFiles();
        return files != null ?
                Arrays.stream(files)
                        .filter(f -> f.isFile() && f.getName().endsWith(extension))
                        .map(File::toPath)
                        .collect(Collectors.toList()) :
                new ArrayList<>();
    }
    /**
     * Creates symbolic links from an input path.
     * If the input path is a folder, links will be created for all its files and sub-folders.
     * @param sourcePath    The input path
     * @param targetPath    The link location
     * @return              The link location
     */
    public static Path link(Path sourcePath, final Path targetPath) throws IOException {
        return link(sourcePath, targetPath, null, null);
    }
    /**
     * Creates symbolic links from an input path, optionally specifying filters for files and sub-folders.
     * If the input path is a folder, links will be created for all its files and sub-folders.
     * @param sourcePath    The input path
     * @param targetPath    The link location
     * @param fileFilter    The filter predicate for files
     * @param folderFilter  The filter predicate for folders
     * @return              The link location
     */
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

    /**
     * Checks if the given path is accessible (i.e., it exists and it is readable)
     * @param path  The path to check
     */
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
    /**
     * Checks if the given path is writeable
     * @param path  The path to check
     */
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

    /**
     * Replaces symbolic links from a path with actual paths.
     * A link may be also part of the path, not only the final part.
     * @param path  The path to resolve.
     * @return      The actual absolute path
     */
    public static Path resolveSymLinks(Path path) throws IOException {
        Path realPath = path.getRoot();
        // If the root is null, the path is relative
        if (realPath != null) {
            final int nameCount = path.getNameCount();
            for (int i = 0; i < nameCount; i++) {
                realPath = realPath.resolve(path.getName(i));
                if (Files.isSymbolicLink(realPath)) {
                    realPath = Files.readSymbolicLink(realPath);
                } else {
                    realPath = realPath.toAbsolutePath();
                }
            }
        }
        return realPath != null ? realPath : path;
    }

    /**
     * Creates a symbolic link of a path in a target path.
     * @param sourcePath    The target of the symbolic link
     * @param file          The path of the symlink to create (if it doesn't exist)
     * @return              The symlink path
     */
    public static Path linkFile(Path sourcePath, Path file) throws IOException {
        return Files.exists(file) ? file : Files.createSymbolicLink(file, sourcePath);
    }
    /**
     * Creates a symbolic link to a folder, making sure that the given path is a folder.
     * @param sourcePath    The source folder
     * @param target        The location of the symlink.
     * @return              The path of the symlink
     */
    public static Path linkFolder(Path sourcePath, Path target) throws IOException {
        if (!Files.isDirectory(sourcePath)) {
            throw new IOException(sourcePath + " is not a directory");
        }
        if (!Files.isDirectory(target)) {
            throw new IOException(target + " is not a directory");
        }
        return linkFile(sourcePath, target);
    }

    /**
     * Replaces a symbolic link with the actual file or folder (i.e., copying it instead of the link).
     * @param link              The symbolic link path
     * @param progressListener  A progress listener
     * @return                  The resolved link path
     */
    public static Path replaceLink(Path link, ProgressListener progressListener) throws IOException {
        if (!Files.exists(link)) {
            throw new NoSuchFileException(link.toString());
        }
        Path resolved;
        if (Files.isSymbolicLink(link)) {
            resolved = Files.readSymbolicLink(link);
            Path target = link.getParent();
            Files.delete(link);
            if (progressListener != null) {
                copy(resolved, target, progressListener);
            } else {
                copy(resolved, target);
            }
        } else {
            resolved = link;
        }
        return resolved;
    }

    /**
     * Returns the UNIX representation of a path.
     * @param path          The path to convert
     * @param insideDocker  If <code>false</code>, on Windows it strips the partition letter.
     */
    public static String asUnixPath(Path path, boolean insideDocker) {
        if (SystemUtils.IS_OS_UNIX) {
            return path.toString();
        } else {
            String strPath;
            if (path.isAbsolute() || path.startsWith("\\")) {
                final URI uri = path.toAbsolutePath().toUri();
                final String stringValue = uri.getRawPath();
                strPath = StringUtils.replaceEach(stringValue, WIN_TO_UNIX_TOKENS, WIN_TO_UNIX_REPLACEMENTS);
                if (insideDocker && strPath.charAt(2) == '/') {
                    strPath = strPath.substring(2);
                }
            } else {
                strPath = "/" +
                        StringUtils.replaceEach(path.toString(), WIN_TO_UNIX_TOKENS, WIN_TO_UNIX_REPLACEMENTS) +
                        (Files.isDirectory(path) ? "/" : "");
            }
            return strPath;
        }
    }
    /**
     * Returns the UNIX representation of a path as a string.
     * @param path          The path to convert
     * @param insideDocker  If <code>false</code>, on Windows it strips the partition letter.
     */
    public static String asUnixPath(String path, boolean insideDocker) {
        if (SystemUtils.IS_OS_UNIX) {
            return path;
        } else {
            String strPath;
            Path pPath = Paths.get(path);
            if (pPath.isAbsolute() || pPath.startsWith("\\")) {
                final URI uri = pPath.toAbsolutePath().toUri();
                final String stringValue = uri.getRawPath();
                strPath = StringUtils.replaceEach(stringValue, WIN_TO_UNIX_TOKENS, WIN_TO_UNIX_REPLACEMENTS);
                if (insideDocker && strPath.charAt(2) == '/') {
                    strPath = strPath.substring(2);
                }
            } else {
                strPath = "/" +
                        StringUtils.replaceEach(path, WIN_TO_UNIX_TOKENS, WIN_TO_UNIX_REPLACEMENTS) +
                        (Files.isDirectory(pPath) ? "/" : "");
            }
            return strPath;
        }
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
    /**
     * Copies the file or folder in the given destination with progress reporting.
     * @param source        The source file or folder
     * @param destination   The destination folder
     * @param listener      The progress listener
     * @return              The number of actual copied files.
     */
    public static int copy(Path source, Path destination, ProgressListener listener) throws IOException {
        final AtomicInteger copied = new AtomicInteger(0);
        try {
            if (source != null && destination != null) {
                listener.started(source.getFileName().toString());
                if (Files.isRegularFile(source)) {
                    copyFileWithProgress(source, destination, listener);
                } else {
                    Path targetFolder = destination.resolve(source.getFileName());
                    Files.createDirectories(targetFolder);
                    double total = folderSize(source);
                    AtomicLong current = new AtomicLong(0);
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
                            Files.copy(file, targetFile);
                            listener.notifyProgress((double) current.addAndGet(Files.size(file)) / total);
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
        } finally {
            listener.ended();
        }
    }

    private static void copyFileWithProgress(Path source, Path destination, ProgressListener listener) throws IOException {
        Files.deleteIfExists(destination);
        byte[] buffer = new byte[262144];
        try (InputStream inStream = new ListenableInputStream(Files.newInputStream(source), listener);
             OutputStream outStream = Files.newOutputStream(destination, StandardOpenOption.CREATE)) {
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        }
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
                Files.move(source,
                           Files.isDirectory(destination) ? destination.resolve(source.getFileName()) : destination,
                           StandardCopyOption.REPLACE_EXISTING);
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

    public static void rename(Path source, String newFileName) throws IOException {
        if (source != null && !StringUtilities.isNullOrEmpty(newFileName)) {
            final Path target = source.getParent().resolve(newFileName);
            if (Files.exists(target)) {
                throw new IOException("Target exists");
            }
            if (Files.isRegularFile(source)) {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.createDirectory(target);
                Files.walkFileTree(source, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if (!dir.equals(source)) {
                            Path subFolder = target.resolve(source.relativize(dir));
                            Files.createDirectories(subFolder);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path targetFile = target.resolve(source.relativize(file));
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
                Files.delete(source);
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

    public static void deleteTreeUnix(Path root) throws IOException {
        if (root != null && Files.isDirectory(root)) {
            Executor<?> executor = Executor.create(ExecutorType.PROCESS,
                                                   null,
                                                   new ArrayList<>() {{ add("rm"); add("-rf"); add(root.toString()); }},
                                                   true);
            OutputAccumulator accumulator = new OutputAccumulator();
            executor.setOutputConsumer(accumulator);
            try {
                if (executor.execute(false) != 0) {
                    throw new IOException(accumulator.getOutput());
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
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
        try (Stream<Path> stream = Files.walk(folder)) {
            return stream.filter(f -> Files.isRegularFile(f, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(f))
                         .mapToLong(p -> p.toFile().length())
                         .sum();
        }
    }

    public static String toReadableString(long value) {
        final int unitIndex = (int) (Math.log10(value) / 3);
        final double unitValue = 1 << (unitIndex * 10);
        return UNIT_FORMAT.format(value / unitValue) + " " + UNITS[unitIndex];
    }

    /**
     * Copies the file from the given URL in the destination file
     * @param sourceURL         The source URL
     * @param destinationFile   The target file
     */
    public static void copyFile(URL sourceURL, Path destinationFile) throws IOException {
        if (sourceURL == null || destinationFile == null) {
            throw new IllegalArgumentException("One of the arguments is null");
        }
        try (InputStream inputStream = sourceURL.openStream()) {
            Files.createDirectories(destinationFile.getParent());
            try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(destinationFile))) {
                byte[] buffer = new byte[65536];
                int read;
                while ((read = inputStream.read(buffer)) > 0) {
                    bos.write(buffer, 0, read);
                }
            }
        }
    }

    /**
     * Uncompresses a zip file in a target folder, optionally keeping the folder structure from inside the zip file
     * @param sourceFile            The source zip file
     * @param destination           The destination folder
     * @param keepFolderStructure   To keep or not to keep the folder structure from the archive
     */
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
                                    Files.newOutputStream((keepFolderStructure
                                                           ? filePath.toFile()
                                                           : strippedFilePath.toFile()).toPath()))) {
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

    /**
     * Finds the common part of two paths. At least the second path must be relative.
     *
     * @param path1 The first path (it may be absolute or relative)
     * @param path2 The second path (it must be relative)
     */
    public static Path findCommonPath(Path path1, Path path2) {
        if (path1 == null || path2 == null || path2.isAbsolute()) {
            return null;
        }
        final int names = path1.getNameCount();
        Path common = null;
        for (int i = 0; i < names; i++) {
            if (common == null) {
                if (path1.getName(i).equals(path2.getName(0))) {
                    common = path1.getName(i);
                }
            } else {
                if (path2.startsWith(common.resolve(path1.getName(i)))) {
                    common = common.resolve(path1.getName(i));
                } else {
                    break;
                }
            }
        }
        return common;
    }
    /**
     * Finds the common part of two paths given as strings. At least the second path must be relative.
     *
     * @param strPath1 The first path (it may be absolute or relative)
     * @param strPath2 The second path (it must be relative)
     */
    public static String findCommonPath(String strPath1, String strPath2) {
        if (StringUtilities.isNullOrEmpty(strPath1) || StringUtilities.isNullOrEmpty(strPath2)) {
            return null;
        }
        final String[] path1 = strPath1.split("/");
        final String[] path2 = strPath2.split("/");
        List<String> common = null;
        int idx = 0;
        for (String s : path1) {
            if (common == null) {
                if (idx == path2.length - 1) {
                    break;
                }
                if (s.equals(path2[idx++])) {
                    common = new ArrayList<>();
                    common.add(s);
                }
            } else {
                if (s.equals(path2[idx++])) {
                    common.add(s);
                } else {
                    common.clear();
                    break;
                }
            }
        }
        return common != null ? String.join("/", common) : null;
    }

    /**
     * Appends a path fragment to a parent path.
     * The child fragment must be relative.
     * @param parent    The parent path
     * @param child     The child path
     */
    public static String resolve(String parent, String child) {
        if (StringUtilities.isNullOrEmpty(child)) {
            return parent;
        } else {
            final String p = parent.replace("\\", "/");
            final String c = child.replace("\\", "/");
            return p.endsWith("/") ? p + c : p + "/" + c;
        }
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        try (ReadableByteChannel inputChannel = Channels.newChannel(input);
             WritableByteChannel outputChannel = Channels.newChannel(output)) {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(65536);
            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.compact();
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                outputChannel.write(buffer);
            }
        }
    }

    public static void appendToStream(InputStream input, OutputStream output) throws IOException {
        WritableByteChannel outputChannel = Channels.newChannel(output);
        try (ReadableByteChannel inputChannel = Channels.newChannel(input)) {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(65536);
            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.compact();
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                outputChannel.write(buffer);
            }
        }
    }
}
