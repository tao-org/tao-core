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
package ro.cs.tao.datasource.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Simple utility class for zipping downloaded products
 *
 * @author Cosmin Cara
 */
public class Zipper {

    public static void compress(Path sourceFolder, String archiveName, boolean deleteFolder) throws IOException {
        Path zipFile = sourceFolder.getParent().resolve(archiveName + ".zip");
        Files.deleteIfExists(zipFile);
        zipFile = Files.createFile(zipFile);
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
                        Logger.getLogger(Zipper.class.getName()).severe(e.getMessage());
                    }
                }
            }
        }
        if (deleteFolder) {
            delete(sourceFolder);
        }
    }

    public static Path decompressZip(Path source, Path target, boolean deleteAfterDecompress) {
        if (source == null || !source.toString().endsWith(".zip")) {
            return null;
        }
        try {
            Files.createDirectories(target);
            byte buffer[] = new byte[65536];
            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(source))) {
                ZipEntry entry = zis.getNextEntry();
                while (entry != null) {
                    Path newFile = target.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(newFile);
                    } else {
                        OutputStream outputStream = Files.newOutputStream(newFile);
                        int read;
                        while ((read = zis.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, read);
                        }
                        outputStream.close();
                    }
                    entry = zis.getNextEntry();
                }
                zis.closeEntry();
            }
            if (deleteAfterDecompress) {
                Files.delete(source);
            }
        } catch (IOException e) {
            Logger.getLogger(Zipper.class.getSimpleName()).warning(e.getMessage());
            return null;
        }
        return target;
    }

    public static Path decompressTarGz(Path source, Path target, boolean deleteAfterDecompress) {
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
                        byte buffer[] = new byte[65536];
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
            Logger.getLogger(Zipper.class.getSimpleName()).warning(ex.getMessage());
            return null;
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
            Logger.getLogger(Zipper.class.getName()).severe(e.getMessage());
            retVal = false;
        }
        return retVal;
    }

}
