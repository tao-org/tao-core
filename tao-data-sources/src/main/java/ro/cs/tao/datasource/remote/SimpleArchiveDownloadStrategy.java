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

package ro.cs.tao.datasource.remote;

import org.apache.http.client.methods.CloseableHttpResponse;
import ro.cs.tao.datasource.InterruptedException;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.util.HttpMethod;
import ro.cs.tao.datasource.util.NetUtils;
import ro.cs.tao.datasource.util.Zipper;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.utils.FileUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class SimpleArchiveDownloadStrategy extends DownloadStrategy {
    private static final long DOWNLOAD_TIMEOUT = 30000; // 30s
    private Timer timeoutTimer;

    public SimpleArchiveDownloadStrategy(String targetFolder, Properties properties) {
        super(targetFolder, properties);
        this.fetchMode = FetchMode.OVERWRITE;
    }

    protected SimpleArchiveDownloadStrategy(SimpleArchiveDownloadStrategy other) {
        super(other);
        this.fetchMode = FetchMode.OVERWRITE;
    }

    @Override
    public SimpleArchiveDownloadStrategy clone() {
        return new SimpleArchiveDownloadStrategy(this);
    }

    @Override
    protected Path fetchImpl(EOProduct product) throws IOException, InterruptedException {
        checkCancelled();
        Path productFile;
        if (currentProduct == null) {
            currentProduct = product;
        }
        String productUrl = getProductUrl(product);
        String extension;
        // It is possible that, for archives, the query string of the URL to contain some authentication/redirection tokens
        if (productUrl.indexOf('?') > 0) {
            String trimmedUrl = productUrl.substring(0, productUrl.indexOf('?'));
            extension = trimmedUrl.endsWith(".zip") ? ".zip" : trimmedUrl.endsWith(".tar.gz") ? ".tar.gz" : "";
        } else {
            extension = productUrl.endsWith(".zip") ? ".zip" : productUrl.endsWith(".tar.gz") ? ".tar.gz" : "";
        }
        boolean isArchive = !extension.isEmpty();
        try (CloseableHttpResponse response = NetUtils.openConnection(HttpMethod.GET, productUrl, null)) {
            int statusCode = response.getStatusLine().getStatusCode();
            logger.finest(String.format("%s returned http code %s", productUrl, statusCode));
            switch (statusCode) {
                case 200:
                    try {
                        subActivityStart(product.getName());
                        Path archivePath = Paths.get(destination, product.getName() + extension);
                        FileUtilities.ensureExists(Paths.get(destination));
                        Files.deleteIfExists(archivePath);
                        SeekableByteChannel outputStream = null;
                        long length = response.getEntity().getContentLength();
                        long size = currentProduct.getApproximateSize();
                        if (size > length) {
                            Path existingProduct = Paths.get(destination, product.getName() + ".SAFE");
                            if (Files.exists(existingProduct)) {
                                long existingSize = FileUtilities.folderSize(existingProduct);
                                logger.fine(String.format("Product %s found: %s; size: %d, expected: %s",
                                                          product.getName(), existingProduct, existingSize, size));
                                if (existingSize >= size) {
                                    logger.fine("Download will be skipped");
                                    try {
                                        product.setLocation(existingProduct.toUri().toString());
                                    } catch (URISyntaxException e) {
                                        logger.severe(e.getMessage());
                                    }
                                    return existingProduct;
                                } else {
                                    logger.fine("Download will be attempted again");
                                }
                            }
                        }
                        currentProduct.setApproximateSize(length);
                        currentProductProgress = new ProductProgress(currentProduct.getApproximateSize(), isArchive);
                        try (InputStream inputStream = response.getEntity().getContent()) {
                            outputStream = Files.newByteChannel(archivePath, EnumSet.of(StandardOpenOption.CREATE,
                                                                                        StandardOpenOption.APPEND,
                                                                                        StandardOpenOption.WRITE));
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int read;
                            logger.finest("Begin reading from input stream");
                            if (this.timeoutTimer == null) {
                                this.timeoutTimer = new Timer("Timeout");
                            }
                            TimerTask task;
                            while (!isCancelled() && (read = inputStream.read(buffer)) != -1) {
                                task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        logger.warning(String.format("Remote host did not send anything for %d seconds, cancelling download",
                                                                     DOWNLOAD_TIMEOUT / 1000));
                                        SimpleArchiveDownloadStrategy.this.cancel();
                                    }
                                };
                                this.timeoutTimer.schedule(task, DOWNLOAD_TIMEOUT);
                                outputStream.write(ByteBuffer.wrap(buffer, 0, read));
                                currentProductProgress.add(read);
                                task.cancel();
                                this.timeoutTimer.purge();
                            }
                            outputStream.close();
                            logger.finest("End reading from input stream");
                            checkCancelled();
                            productFile = extract(archivePath, computeTarget(archivePath));
                            if (productFile != null) {
                                try {
                                    product.setLocation(productFile.toUri().toString());
                                } catch (URISyntaxException e) {
                                    logger.severe(e.getMessage());
                                }
                            }
                        } finally {
                            if (outputStream != null && outputStream.isOpen()) outputStream.close();
                            if (this.timeoutTimer != null) {
                                this.timeoutTimer.cancel();
                                this.timeoutTimer = null;
                            }
                        }
                        logger.fine(String.format("End download for %s", product.getName()));
                    } finally {
                        subActivityEnd(product.getName());
                    }
                    break;
                case 401:
                    throw new QueryException("The supplied credentials are invalid!");
                default:
                    throw new QueryException(String.format("The request was not successful. Reason: %s",
                                                           response.getStatusLine().getReasonPhrase()));
            }
        }
        return productFile;
    }

    protected Path computeTarget(Path archivePath) {
        return archivePath.getFileName().toString().endsWith(".tar.gz") ?
                Paths.get(archivePath.toString().replace(".tar.gz", "")) :
                Paths.get(archivePath.toString().replace(".zip", ""));
    }

    protected Path extract(Path archivePath, Path targetPath) {
        logger.fine(String.format("Begin decompressing %s into %s", archivePath.getFileName(), targetPath));
        Path result = archivePath.toString().endsWith(".tar.gz") ?
            Zipper.decompressTarGz(archivePath, targetPath, true) :
            Zipper.decompressZip(archivePath, targetPath, true);
        logger.fine(String.format("Decompression of %s completed",archivePath.getFileName()));
        return result;
    }

    @Override
    protected String getMetadataUrl(EOProduct descriptor) {
        throw new RuntimeException("Metadata file not supported for this strategy");
    }
}
