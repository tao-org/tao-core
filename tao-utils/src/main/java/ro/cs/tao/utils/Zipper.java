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
package ro.cs.tao.utils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import ro.cs.tao.utils.executors.MemoryUnit;
import ro.cs.tao.utils.executors.monitoring.ProgressListener;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Simple utility class for zipping downloaded products
 *
 * @author Cosmin Cara
 */
public class Zipper {

    private static final String TAR_MAGIC = "ustar";
    private static final Logger logger = Logger.getLogger(Zipper.class.getName());

    public static void main(String[] args) throws IOException {
        Path source = Paths.get("D:\\download\\rou_2018_s1_109\\S1B_IW_SLC__1SDV_20181103T042027_20181103T042055_013435_018DC1_2CCB.zip");
        Path target = Paths.get("D:\\download\\rou_2018_s1_109");
        decompressZip(source, target, false);
    }

    /**
     * Compresses the contents of a folder into a zip archive.
     *
     * @param sourceFolder   The path of the folder to compress.
     * @param archiveName    The name of the resulting zip archive (without extension).
     * @param deleteFolder   If true, deletes the original folder after compression.
     * @param listener       An optional {@link ProgressListener} to receive progress updates
     *                       and status of the compression process.
     * @throws IOException   If an I/O error occurs during compression.
     */
    public static void compress(Path sourceFolder, String archiveName, boolean deleteFolder, ProgressListener listener) throws IOException {
        Path zipFile = sourceFolder.getParent().resolve(archiveName + ".zip");
        Files.deleteIfExists(zipFile);
        Files.createFile(zipFile);
        try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            try (Stream<Path> files = Files.walk(sourceFolder, FileVisitOption.FOLLOW_LINKS)) {
                final List<Path> paths = files.collect(Collectors.toList());
                final long total = listener != null
                        ? paths.stream().filter(Files::isRegularFile).mapToLong(value -> {
                                try {
                                    return Files.size(value);
                                } catch (IOException e) {
                                    return 0;
                                }
                            }).sum()
                        : -1;
                if (listener != null) {
                    listener.started(archiveName);
                }
                long read = 0;
                int iterations = 0;
                for (Path path : paths) {
                    String sp = sourceFolder.relativize(path).toString();
                    try {
                        ZipEntry entry = new ZipEntry(sp);
                        outputStream.putNextEntry(entry);
                        if (!Files.isDirectory(path)) {
                            byte[] buffer = new byte[(int) MemoryUnit.MB.value()];
                            try (InputStream stream = Files.newInputStream(path)) {
                                int r;
                                while ((r = stream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, r);
                                    read += r;
                                    if (listener != null && iterations++ % 64 == 0) { // report progress at each 64MB read
                                        listener.notifyProgress((double) read / (double) total);
                                    }
                                }
                            }
                        }
                        outputStream.closeEntry();
                    } catch (Exception e) {
                        logger.severe(e.getMessage());
                    }
                }
                if (listener != null) {
                    listener.ended();
                }
            }
        }
        if (deleteFolder) {
            delete(sourceFolder);
        }
    }

    /**
     * Decompresses a ZIP file into a specified target directory. Optionally deletes
     * the ZIP file after decompression.
     *
     * @param source               The path to the ZIP file to decompress. Must not be null and should end with ".zip".
     * @param target               The path to the target directory where the decompressed files will be extracted.
     * @param deleteAfterDecompress If true, deletes the source ZIP file after successful decompression.
     * @return The path to the target directory where the files were decompressed. Returns null if the source path
     *         is invalid or not a ZIP file.
     * @throws IOException If an I/O error occurs during the decompression process.
     */
    public static Path decompressZip(Path source, Path target, boolean deleteAfterDecompress) throws IOException {
        if (source == null || !source.toString().endsWith(".zip")) {
            return null;
        }
        try {
            FileUtilities.createDirectories(target);
            byte[] buffer = new byte[65536];
            try (InputStream is = Files.newInputStream(source);
                 ZipInputStream zis = new ZipInputStream(is)) {
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    Path newFile = target.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        FileUtilities.createDirectories(newFile);
                    } else {
                        FileUtilities.createDirectories(newFile.getParent());
                        try (OutputStream outputStream = Files.newOutputStream(newFile)) {
                            int read;
                            while ((read = zis.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, read);
                            }
                        }
                    }
                    entry = zis.getNextEntry();
                }
                zis.closeEntry();
            }
            if (deleteAfterDecompress) {
                Files.delete(source);
            }
        } catch (IOException e) {
            logger.warning(ExceptionUtils.getStackTrace(logger, e));
            throw e;
        }
        return target;
    }

    /**
     * Decompresses a specified ZIP file into a target directory and reports progress using an optional listener.
     *
     * @param source   The path to the ZIP file to decompress. Must not be null and should end with ".zip".
     * @param target   The path to the target directory where the decompressed files will be extracted.
     * @param listener An optional {@link ProgressListener} for receiving progress updates during the decompression process.
     * @return The path to the target directory where the files were decompressed. Returns null if the source path
     *         is invalid or not a ZIP file.
     * @throws IOException If an I/O error occurs during the decompression process.
     */
    public static Path decompressZip(Path source, Path target, ProgressListener listener) throws IOException {
        if (source == null || !source.toString().endsWith(".zip")) {
            return null;
        }
        try {
            FileUtilities.createDirectories(target);
            byte[] buffer = new byte[(int) MemoryUnit.MB.value()];
            double totalRead = 0;
            long total = Files.size(source);
            int iterations = 0;
            if (listener != null) {
                listener.started(source.getFileName().toString());
            }
            try (InputStream is = Files.newInputStream(source);
                 ZipInputStream zis = new ZipInputStream(is)) {
                ZipEntry entry = zis.getNextEntry();
                if (entry == null && isTar(source)) {
                    decompressTar(source, target);
                } else {
                    while (entry != null) {
                        Path newFile = target.resolve(entry.getName());
                        if (entry.isDirectory()) {
                            FileUtilities.createDirectories(newFile);
                        } else {
                            FileUtilities.createDirectories(newFile.getParent());
                            try (OutputStream outputStream = Files.newOutputStream(newFile)) {
                                int read;
                                while ((read = zis.read(buffer)) > 0) {
                                    outputStream.write(buffer, 0, read);
                                    totalRead += (double) read * ((double) entry.getCompressedSize() / entry.getSize());
                                    if (listener != null && iterations++ % 128 == 0) { // report progress at each 128MB read
                                        listener.notifyProgress(totalRead / total);
                                    }
                                }
                            }
                        }
                        entry = zis.getNextEntry();
                    }
                    zis.closeEntry();
                }
            }
        } catch (IOException e) {
            logger.warning(ExceptionUtils.getStackTrace(logger, e));
            throw e;
        } finally {
            if (listener != null) {
                listener.ended();
            }
        }
        return target;
    }

    /**
     * Decompresses a ZIP file into a specified target directory using a multi-threaded approach.
     * Optionally creates a folder named after the ZIP file at the target location and
     * deletes the source ZIP file after successful decompression.
     *
     * @param source               The path to the ZIP file to decompress. Must not be null and should end with ".zip".
     * @param target               The path to the target directory where the decompressed files will be extracted.
     * @param createFolderFromName If true, creates a folder named after the ZIP file at the target location
     *                             and extracts the files into it. If false, extracts files directly into the target directory.
     * @param deleteAfterDecompress If true, deletes the source ZIP file after successful decompression.
     * @return The path to the target directory where the files were decompressed. Returns null if the source path
     *         is invalid or not a ZIP file.
     */
    public static Path decompressZipMT(Path source, Path target, boolean createFolderFromName, boolean deleteAfterDecompress) {
        if (source == null || !source.toString().endsWith(".zip")) {
            return null;
        }
        final AtomicBoolean hasError = new AtomicBoolean(false);
        final Path root = createFolderFromName ? target : target.getParent();
        try (ZipFile zipFile = new ZipFile(source.toFile())) {
            FileUtilities.createDirectories(root);
            zipFile.stream().parallel().forEach(entry -> {
                final Path newFile = root.toAbsolutePath().resolve(entry.getName());
                try {
                    if (entry.isDirectory()) {
                        FileUtilities.createDirectories(newFile);
                    } else {
                        int read;
                        final byte[] buffer = new byte[65536];
                        try (InputStream inputStream = zipFile.getInputStream(entry)) {
                            FileUtilities.createDirectories(newFile.getParent());
                            try (OutputStream outputStream = Files.newOutputStream(newFile)) {
                                while ((read = inputStream.read(buffer)) > 0) {
                                    outputStream.write(buffer, 0, read);
                                }
                            }
                        }
                    }
                } catch (IOException inner) {
                    logger.warning(ExceptionUtils.getStackTrace(logger, inner));
                    hasError.set(true);
                    try {
                        Files.deleteIfExists(newFile);
                    } catch (IOException e) {
                        logger.warning(ExceptionUtils.getStackTrace(logger, e));
                    }
                }
            });
        } catch (IOException ex) {
            logger.warning(ExceptionUtils.getStackTrace(logger, ex));
            hasError.set(true);
            return null;
        } finally {
            if (deleteAfterDecompress && !hasError.get()) {
                try {
                    Files.delete(source);
                } catch (IOException e) {
                    logger.warning(ExceptionUtils.getStackTrace(logger, e));
                }
            }
        }
        return target;
    }

    public static Path decompressTar(Path source, Path target) throws IOException {
        if (source == null) {
            return null;
        }
        try (InputStream is = Files.newInputStream(source);
             TarArchiveInputStream tarStream = new TarArchiveInputStream(is)) {
            TarArchiveEntry entry;
            while ((entry = tarStream.getNextTarEntry()) != null) {
                final Path currentPath = target.resolve(entry.getName());
                if (entry.isDirectory()) {
                    FileUtilities.createDirectories(currentPath);
                } else {
                    int count;
                    final byte[] buffer = new byte[65536];
                    FileUtilities.createDirectories(currentPath.getParent());
                    try (OutputStream outputStream = Files.newOutputStream(currentPath)) {
                        while ((count = tarStream.read(buffer, 0, buffer.length)) != -1) {
                            outputStream.write(buffer, 0, count);
                        }
                    }
                }
            }
        }
        return target;
    }

    /**
     * Decompresses a tar.gz (or tar) file into a specified target directory. Optionally deletes
     * the source file after successful decompression.
     *
     * @param source               The path to the tar.gz (or tar) file to decompress. Must not be null and should end with ".tar.gz".
     *                             If the source is not valid, the method returns null.
     * @param target               The path to the target directory where the decompressed files will be extracted.
     *                             The directory is created if it does not already exist.
     * @param deleteAfterDecompress If true, deletes the source file after successful decompression.
     * @return The path to the target directory where the files were decompressed. Returns null if the source path
     *         is invalid or not a tar.gz file.
     * @throws IOException If an I/O error occurs during the decompression process.
     */
    public static Path decompressTarGz(Path source, Path target, boolean deleteAfterDecompress) throws IOException {
        if (source == null || !source.toString().endsWith(".tar.gz")) {
            return null;
        }
        try {
            FileUtilities.createDirectories(target);
            try (InputStream is = Files.newInputStream(source);
                 TarArchiveInputStream tarStream = new TarArchiveInputStream(new GzipCompressorInputStream(is))) {
                TarArchiveEntry entry;
                while ((entry = tarStream.getNextTarEntry()) != null) {
                    final Path currentPath = target.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        FileUtilities.createDirectories(currentPath);
                    } else {
                        int count;
                        final byte[] buffer = new byte[65536];
                        FileUtilities.createDirectories(currentPath.getParent());
                        try (OutputStream outputStream = Files.newOutputStream(currentPath)) {
                            while ((count = tarStream.read(buffer, 0, buffer.length)) != -1) {
                                outputStream.write(buffer, 0, count);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                if (e.getMessage().equals("Input is not in the .gz format")) {
                    decompressTar(source, target);
                }
            }
            if (deleteAfterDecompress) {
                Files.delete(source);
            }
        } catch (IOException ex) {
            logger.warning(ex.getMessage());
            throw ex;
        }
        return target;
    }

    public static boolean isTar(Path file) throws IOException {
        if (file == null || !Files.exists(file) || Files.isDirectory(file)) {
            return false;
        }
        final StringBuilder buffer = new StringBuilder();
        try (InputStream is = Files.newInputStream(file);
             Reader reader = new BufferedReader(new InputStreamReader(is))) {
                 reader.skip(256);
                 int read;
                 for (int i = 0; i < 4; i++) {
                     if ((read = reader.read()) != -1) {
                         buffer.append((char) read);
                     } else {
                         break;
                     }
                 }
                 return TAR_MAGIC.equals(buffer.toString());
        }
    }

    private static boolean delete(Path path) {
        if (!Files.exists(path)) {
            return false;
        }
        boolean retVal = false;
        try {
            if (Files.isDirectory(path)) {
                Files.walkFileTree(path, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                retVal = Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        return retVal;
    }

}
