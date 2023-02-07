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
package ro.cs.tao.datasource.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import ro.cs.tao.utils.ExceptionUtils;
import ro.cs.tao.utils.executors.MemoryUnit;
import ro.cs.tao.utils.executors.monitoring.ProgressListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private static final Logger logger = Logger.getLogger(Zipper.class.getName());

    public static void main(String[] args) throws IOException {
        Path source = Paths.get("D:\\download\\rou_2018_s1_109\\S1B_IW_SLC__1SDV_20181103T042027_20181103T042055_013435_018DC1_2CCB.zip");
        Path target = Paths.get("D:\\download\\rou_2018_s1_109");
        decompressZip(source, target, false);
    }

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
                for (Path path : paths) {
                    String sp = sourceFolder.relativize(path).toString();
                    try {
                        ZipEntry entry = new ZipEntry(sp);
                        outputStream.putNextEntry(entry);
                        if (!Files.isDirectory(path)) {
                            byte[] buffer = new byte[MemoryUnit.MB.value().intValue()];
                            try (InputStream stream = Files.newInputStream(path)) {
                                int r;
                                while ((r = stream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, r);
                                    if (listener != null) {
                                        read += r;
                                        listener.notifyProgress((double) r / (double) total);
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

    public static Path decompressZip(Path source, Path target, boolean deleteAfterDecompress) throws IOException {
        if (source == null || !source.toString().endsWith(".zip")) {
            return null;
        }
        try {
            Files.createDirectories(target);
            byte[] buffer = new byte[65536];
            try (InputStream is = Files.newInputStream(source);
                 ZipInputStream zis = new ZipInputStream(is)) {
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    Path newFile = target.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(newFile);
                    } else {
                        Files.createDirectories(newFile.getParent());
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

    public static Path decompressZip(Path source, Path target, ProgressListener listener) throws IOException {
        if (source == null || !source.toString().endsWith(".zip")) {
            return null;
        }
        try {
            Files.createDirectories(target);
            byte[] buffer = new byte[65536];
            double totalRead = 0;
            long total = Files.size(source);
            try (InputStream is = Files.newInputStream(source);
                 ZipInputStream zis = new ZipInputStream(is)) {
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    Path newFile = target.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(newFile);
                    } else {
                        Files.createDirectories(newFile.getParent());
                        try (OutputStream outputStream = Files.newOutputStream(newFile)) {
                            int read;
                            while ((read = zis.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, read);
                                totalRead += (double) read * ((double) entry.getCompressedSize() / entry.getSize());
                                if (listener != null) {
                                    listener.notifyProgress(totalRead / total);
                                }
                            }
                        }
                    }
                    entry = zis.getNextEntry();
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            logger.warning(ExceptionUtils.getStackTrace(logger, e));
            throw e;
        }
        return target;
    }

    public static Path decompressZipMT(Path source, Path target, boolean createFolderFromName, boolean deleteAfterDecompress) {
        if (source == null || !source.toString().endsWith(".zip")) {
            return null;
        }
        final AtomicBoolean hasError = new AtomicBoolean(false);
        final Path root = createFolderFromName ? target : target.getParent();
        try (ZipFile zipFile = new ZipFile(source.toFile())) {
            Files.createDirectories(root);
            zipFile.stream().parallel().forEach(entry -> {
                final Path newFile = root.toAbsolutePath().resolve(entry.getName());
                try {
                    if (entry.isDirectory()) {
                        Files.createDirectories(newFile);
                    } else {
                        int read;
                        final byte[] buffer = new byte[65536];
                        try (InputStream inputStream = zipFile.getInputStream(entry)) {
                            Files.createDirectories(newFile.getParent());
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

    public static Path decompressTarGz(Path source, Path target, boolean deleteAfterDecompress) throws IOException {
        if (source == null || !source.toString().endsWith(".tar.gz")) {
            return null;
        }
        try {
            Files.createDirectories(target);
            try (InputStream is = Files.newInputStream(source);
                 TarArchiveInputStream tarStream = new TarArchiveInputStream(new GzipCompressorInputStream(is))) {
                TarArchiveEntry entry;
                while ((entry = tarStream.getNextTarEntry()) != null) {
                    final Path currentPath = target.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(currentPath);
                    } else {
                        int count;
                        final byte[] buffer = new byte[65536];
                        Files.createDirectories(currentPath.getParent());
                        try (OutputStream outputStream = Files.newOutputStream(currentPath)) {
                            while ((count = tarStream.read(buffer, 0, buffer.length)) != -1) {
                                outputStream.write(buffer, 0, count);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                if (e.getMessage().equals("Input is not in the .gz format")) {
                    try (InputStream is = Files.newInputStream(source);
                         TarArchiveInputStream tarStream = new TarArchiveInputStream(is)) {
                        TarArchiveEntry entry;
                        while ((entry = tarStream.getNextTarEntry()) != null) {
                            final Path currentPath = target.resolve(entry.getName());
                            if (entry.isDirectory()) {
                                Files.createDirectories(currentPath);
                            } else {
                                int count;
                                final byte[] buffer = new byte[65536];
                                Files.createDirectories(currentPath.getParent());
                                try (OutputStream outputStream = Files.newOutputStream(currentPath)) {
                                    while ((count = tarStream.read(buffer, 0, buffer.length)) != -1) {
                                        outputStream.write(buffer, 0, count);
                                    }
                                }
                            }
                        }
                    }
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
