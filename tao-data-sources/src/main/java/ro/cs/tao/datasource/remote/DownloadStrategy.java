/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */

/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */
package ro.cs.tao.datasource.remote;

import ro.cs.tao.ProgressListener;
import ro.cs.tao.datasource.InterruptedException;
import ro.cs.tao.datasource.ProductFetchStrategy;
import ro.cs.tao.datasource.util.NetUtils;
import ro.cs.tao.datasource.util.Utilities;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.notifications.ProgressNotifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Base class for download fetching strategies.
 * It contains an implementation for only fetching given a product descriptor.
 *
 * @author  Cosmin Cara
 */
public abstract class DownloadStrategy implements ProductFetchStrategy {
    private static final String startMessage = "(%s,%s) %s [size: %skB]";
    private static final String completeMessage = "(%s,%s) %s [elapsed: %ss]";
    private static final String errorMessage ="Cannot download %s: %s";
    private static final int BUFFER_SIZE = 1024 * 1024;
    protected static final String NAME_SEPARATOR = "_";
    public static final String URL_SEPARATOR = "/";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    protected Properties props;
    protected String destination;
    private String localArchiveRoot;

    protected EOProduct currentProduct;
    protected double currentProductProgress;
    protected String currentStep;

    private boolean shouldCompress;
    private boolean shouldDeleteAfterCompression;
    private DownloadMode downloadMode;

    private ProgressListener progressListener;
    private volatile boolean cancelled;

    protected Logger logger = Logger.getLogger(DownloadStrategy.class.getName());

    public DownloadStrategy(String targetFolder, Properties properties) {
        this.destination = targetFolder;
        this.props = properties;
    }

    public String getLocalArchiveRoot() {
        return localArchiveRoot;
    }

    public void setLocalArchiveRoot(String localArchiveRoot) {
        this.localArchiveRoot = localArchiveRoot;
    }

    public void setProgressListener(ProgressNotifier progressListener) {
        this.progressListener = progressListener;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    public ReturnCode download(List<EOProduct> products) {
        ReturnCode retCode = ReturnCode.OK;
        if (products != null) {
            for (EOProduct product : products) {
                if (cancelled) {
                    return ReturnCode.INTERRUPTED;
                }
                long startTime = System.currentTimeMillis();
                Path file = null;
                currentProduct = product;
                currentProductProgress = 0;
                try {
                    final Path destPath = Paths.get(destination);
                    Utilities.ensureExists(destPath);
                    switch (this.downloadMode) {
                        case COPY:
                            file = copy(product, Paths.get(localArchiveRoot), destPath);
                            if (file == null) {
                                retCode = ReturnCode.EMPTY;
                                logger.warning("Product copy failed");
                            }
                            break;
                        case SYMLINK:
                            file = link(product, Paths.get(localArchiveRoot), destPath);
                            if (file == null) {
                                retCode = ReturnCode.EMPTY;
                                logger.warning("Product link failed");
                            }
                            break;
                        case OVERWRITE:
                        case RESUME:
                        default:
                            file = fetch(product);
                            if (file == null) {
                                retCode = ReturnCode.EMPTY;
                                logger.warning("Product download aborted");
                            }
                            break;
                    }
                } catch (InterruptedException ignored) {
                } catch (IOException ex) {
                    logger.warning("IO Exception: " + ex.getMessage());
                    logger.warning("Product download failed");
                    retCode = ReturnCode.INTERRUPTED;
                }
                long millis = System.currentTimeMillis() - startTime;
                if (file != null && Files.exists(file)) {
                    logger.info(String.format("Product download completed in %s", Utilities.formatTime(millis)));
                }
            }
        }
        return retCode;
    }

    public String getProductUrl(EOProduct descriptor){
        URI location = null;
        try {
            location = new URI(descriptor.getLocation());
        } catch (URISyntaxException ignored) {
        }
        return location != null ? location.toString() : null;
    }

    void shouldCompress(boolean shouldCompress) {
        this.shouldCompress = shouldCompress;
    }

    void shouldDeleteAfterCompression(boolean shouldDeleteAfterCompression) {
        this.shouldDeleteAfterCompression = shouldDeleteAfterCompression;
    }

    void setDownloadMode(DownloadMode mode) {
        this.downloadMode = mode;
    }

    protected abstract String getMetadataUrl(EOProduct descriptor);

    protected Path downloadFile(String remoteUrl, Path file) throws IOException, InterruptedException {
        return downloadFile(remoteUrl, file, null);
    }

    protected Path downloadFile(String remoteUrl, Path file, String authToken) throws IOException, InterruptedException {
        return downloadFile(remoteUrl, file, this.downloadMode, authToken);
    }

    protected Path findProductPath(Path root, EOProduct product) {
        // Products are assumed to be organized by year (yyyy), month (MM) and day (dd)
        // If it's not the case, this method should be overridden
        String date = dateFormat.format(product.getAcquisitionDate());
        Path productPath = root.resolve(date.substring(0, 4));
        if (Files.exists(productPath)) {
            productPath = productPath.resolve(date.substring(4, 6));
            productPath = Files.exists(productPath) ?
                    productPath.resolve(date.substring(6, 8)).resolve(product.getName()) :
                    null;
            if (productPath != null && !Files.exists(productPath)) {
                productPath = null;
            }
        } else {
            productPath = null;
        }
        return productPath;
    }

    protected Path copy(EOProduct product, Path sourceRoot, Path targetRoot) throws IOException {
        Path sourcePath = findProductPath(sourceRoot, product);
        if (sourcePath == null) {
            logger.warning(String.format("Product %s not found in the local archive", product.getName()));
            return null;
        }
        Path destinationPath = targetRoot.resolve(sourcePath.getFileName());
        if (Files.isDirectory(sourcePath)) {
            if (!Files.exists(destinationPath)) {
                Files.createDirectory(destinationPath);
            }
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (cancelled) {
                        return FileVisitResult.TERMINATE;
                    }
                    Path target = destinationPath.resolve(sourcePath.relativize(dir));
                    if (!Files.exists(target)) {
                        Files.createDirectory(target);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (cancelled) {
                        return FileVisitResult.TERMINATE;
                    }
                    Files.copy(file,
                               destinationPath.resolve(sourcePath.relativize(file)),
                               StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return destinationPath;
    }

    protected Path link(EOProduct product, Path sourceRoot, Path targetRoot) throws IOException {
        Path sourcePath = findProductPath(sourceRoot, product);
        if (sourcePath == null) {
            logger.warning(String.format("Product %s not found in the local archive", product.getName()));
        }
        Path destinationPath = sourcePath != null ? targetRoot.resolve(sourcePath.getFileName()) : null;
        if (destinationPath != null && !Files.exists(destinationPath)) {
            return Files.createSymbolicLink(destinationPath, sourcePath);
        } else {
            return destinationPath;
        }
    }

    private Path downloadFile(String remoteUrl, Path file, DownloadMode mode, String authToken) throws IOException, InterruptedException {
        if (cancelled) {
            throw new InterruptedException();
        }
        String subActivity = remoteUrl.substring(remoteUrl.lastIndexOf(URL_SEPARATOR) + 1);
        HttpURLConnection connection = null;
        try {
            logger.fine(String.format("Begin download for %s", subActivity));
            if (this.progressListener != null) {
                this.progressListener.subActivityStarted(subActivity);
            }
            connection = NetUtils.openConnection(remoteUrl, authToken);
            long remoteFileLength = connection.getContentLengthLong();
            long localFileLength = 0;
            if (cancelled) {
                throw new InterruptedException();
            }
            if (Files.exists(file)) {
                localFileLength = Files.size(file);
                if (localFileLength != remoteFileLength) {
                    if (DownloadMode.RESUME.equals(mode)) {
                        connection.disconnect();
                        connection = NetUtils.openConnection(remoteUrl, authToken);
                        connection.setRequestProperty("Range", "bytes=" + localFileLength + "-");
                    } else {
                        Files.delete(file);
                    }
                    logger.fine(String.format("Remote file size: %s. Local file size: %s. File " +
                                                      (DownloadMode.OVERWRITE.equals(mode) ?
                                                              "will be downloaded again." :
                                                              "download will be resumed."),
                                              remoteFileLength,
                                              localFileLength));
                }
            }
            if (cancelled) {
                throw new InterruptedException();
            }
            if (localFileLength != remoteFileLength) {
                int kBytes = (int) (remoteFileLength / 1024);
                logger.info(String.format(startMessage, currentProduct.getName(), currentStep, file.getFileName(), kBytes));
                InputStream inputStream = null;
                SeekableByteChannel outputStream = null;
                try {
                    logger.fine(String.format("Local temporary file %s created", file.toString()));
                    long start = System.currentTimeMillis();
                    inputStream = connection.getInputStream();
                    outputStream = Files.newByteChannel(file, EnumSet.of(StandardOpenOption.CREATE,
                                                                         StandardOpenOption.APPEND,
                                                                         StandardOpenOption.WRITE));
                    outputStream.position(localFileLength);
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int read;
                    int totalRead = 0;
                    logger.fine("Begin reading from input stream");
                    while (!cancelled && (read = inputStream.read(buffer)) != -1) {
                        outputStream.write(ByteBuffer.wrap(buffer, 0, read));
                        totalRead += read;
                        if (this.progressListener != null) {
                            this.progressListener.notifyProgress(subActivity,
                                                                 (double) totalRead / (double) remoteFileLength);
                        }
                    }
                    logger.fine("End reading from input stream");
                    if (cancelled) {
                        throw new InterruptedException();
                    }
                    logger.info(String.format(completeMessage, currentProduct.getName(), currentStep, file.getFileName(), (System.currentTimeMillis() - start) / 1000));
                } finally {
                    if (outputStream != null) outputStream.close();
                    if (inputStream != null) inputStream.close();
                }
                logger.fine(String.format("End download for %s", remoteUrl));
            } else {
                logger.fine("File already downloaded");
                logger.info(String.format(completeMessage, currentProduct.getName(), currentStep, file.getFileName(), 0));
            }
            currentProductProgress += Math.min(1.0, currentProduct.getApproximateSize() > 0 ?
                    (double) remoteFileLength / (double) currentProduct.getApproximateSize() : 0);
            if (this.progressListener != null) {
                this.progressListener.notifyProgress(currentProductProgress);
            }
        } catch (FileNotFoundException fnex) {
            logger.warning(String.format(errorMessage, remoteUrl, "No such file"));
            file = null;
        } catch (InterruptedIOException iioe) {
            logger.severe("Operation timed out");
            throw new IOException("Operation timed out");
        } catch (Exception ex) {
            logger.severe(String.format(errorMessage, remoteUrl, ex.getMessage()));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (this.progressListener != null) {
                this.progressListener.subActivityEnded(subActivity);
            }
        }
        return Utilities.ensurePermissions(file);
    }
}
