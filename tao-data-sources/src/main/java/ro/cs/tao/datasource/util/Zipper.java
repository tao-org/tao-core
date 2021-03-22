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
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import ro.cs.tao.utils.ExceptionUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
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

    public static void compress(Path sourceFolder, String archiveName, boolean deleteFolder) throws IOException {
        Path zipFile = sourceFolder.getParent().resolve(archiveName + ".zip");
        Files.deleteIfExists(zipFile);
        Files.createFile(zipFile);
        try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            try (Stream<Path> files = Files.walk(sourceFolder)) {
                Iterator<Path> pathIterator = files.iterator();
                while (pathIterator.hasNext()) {
                    Path path = pathIterator.next();
                    String sp = sourceFolder.relativize(path).toString();
                    try {
                        ZipEntry entry = new ZipEntry(sp);
                        outputStream.putNextEntry(entry);
                        if (!Files.isDirectory(path)) {
                            outputStream.write(Files.readAllBytes(path));
                        }
                        outputStream.closeEntry();
                    } catch (Exception e) {
                        logger.severe(e.getMessage());
                    }
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
            try (ZipFile zipFile = new ZipFile(source.toFile())) {
                Files.createDirectories(target);
                final byte[] buffer = new byte[65536];
                final Enumeration<ZipArchiveEntry> zipEntries = zipFile.getEntries();
                while (zipEntries.hasMoreElements()) {
                    final ZipArchiveEntry zipEntry = zipEntries.nextElement();
                    final File entryFile = new File(target.toFile(), zipEntry.getName());
                    if (zipEntry.isDirectory()) {
                        entryFile.mkdirs();
                    } else {
                        logger.finest("Decompressing " + entryFile);
                        entryFile.getParentFile().mkdirs();
                        int read;
                        try (FileOutputStream out = new FileOutputStream(entryFile)) {
                            try (InputStream in = zipFile.getInputStream(zipEntry)) {
                                while ((read = in.read(buffer)) > 0) {
                                    out.write(buffer, 0, read);
                                }
                            }
                        }
                    }
                }
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

    public static Path decompressZipMT(Path source, Path target, boolean createFolderFromName, boolean deleteAfterDecompress) {
        if (source == null || !source.toString().endsWith(".zip")) {
            return null;
        }
        final AtomicBoolean hasError = new AtomicBoolean(false);
        final Path root = createFolderFromName ? target : target.getParent();
        try (ZipFile zipFile = new ZipFile(source.toFile())) {
            Files.createDirectories(root);
            final byte[] buffer = new byte[65536];
            final Enumeration<ZipArchiveEntry> zipEntries = zipFile.getEntries();
            StreamSupport.stream(
                new Spliterators.AbstractSpliterator<ZipArchiveEntry>(Long.MAX_VALUE, Spliterator.ORDERED) {
                    public boolean tryAdvance(Consumer<? super ZipArchiveEntry> action) {
                        if(zipEntries.hasMoreElements()) {
                            action.accept(zipEntries.nextElement());
                            return true;
                        }
                        return false;
                    }
                    public void forEachRemaining(Consumer<? super ZipArchiveEntry> action) {
                        while(zipEntries.hasMoreElements()) action.accept(zipEntries.nextElement());
                    }
                }, false).parallel().forEach(zipEntry -> {
                    final File entryFile = new File(target.toFile(), zipEntry.getName());
                    if (zipEntry.isDirectory()) {
                        entryFile.mkdirs();
                    } else {
                        logger.finest("Decompressing " + entryFile);
                        entryFile.getParentFile().mkdirs();
                        int read;
                        try (FileOutputStream out = new FileOutputStream(entryFile)) {
                            try (InputStream in = zipFile.getInputStream(zipEntry)) {
                                while ((read = in.read(buffer)) > 0) {
                                    out.write(buffer, 0, read);
                                }
                            }
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
        return root;
    }

    public static Path decompressTarGz(Path source, Path target, boolean deleteAfterDecompress) throws IOException {
        if (source == null || !source.toString().endsWith(".tar.gz")) {
            return null;
        }
        try {
            Files.createDirectories(target);
            try (TarArchiveInputStream tarStream = new TarArchiveInputStream(new GzipCompressorInputStream(Files.newInputStream(source)))) {
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
