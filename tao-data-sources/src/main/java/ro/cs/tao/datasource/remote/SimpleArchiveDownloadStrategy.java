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

public class SimpleArchiveDownloadStrategy extends DownloadStrategy {

    public SimpleArchiveDownloadStrategy(String targetFolder, Properties properties) {
        super(targetFolder, properties);
    }

    private SimpleArchiveDownloadStrategy(SimpleArchiveDownloadStrategy other) {
        super(other);
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
        String extension = productUrl.endsWith(".zip") ? ".zip" :
                productUrl.endsWith(".tar.gz") ? ".tar.gz" : "";
        boolean isArchive = !extension.isEmpty();
        try (CloseableHttpResponse response = NetUtils.openConnection(HttpMethod.GET, productUrl, null)) {
            int statusCode = response.getStatusLine().getStatusCode();
            logger.fine(String.format("%s returned http code %s", productUrl, statusCode));
            switch (statusCode) {
                case 200:
                    try {
                        subActivityStart(product.getName());
                        Path archivePath = Paths.get(destination, product.getName() + extension);
                        FileUtilities.ensureExists(Paths.get(destination));
                        Files.deleteIfExists(archivePath);
                        SeekableByteChannel outputStream = null;
                        currentProduct.setApproximateSize(response.getEntity().getContentLength());
                        currentProductProgress = new ProductProgress(product.getApproximateSize(), isArchive);
                        try (InputStream inputStream = response.getEntity().getContent()) {
                            outputStream = Files.newByteChannel(archivePath, EnumSet.of(StandardOpenOption.CREATE,
                                                                                        StandardOpenOption.APPEND,
                                                                                        StandardOpenOption.WRITE));
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int read;
                            int totalRead = 0;
                            logger.fine("Begin reading from input stream");
                            while (!isCancelled() && (read = inputStream.read(buffer)) != -1) {
                                outputStream.write(ByteBuffer.wrap(buffer, 0, read));
                                totalRead += read;
                                currentProductProgress.add(totalRead);
                            }
                            outputStream.close();
                            logger.fine("End reading from input stream");
                            checkCancelled();
                            if (extension.equals(".tar.gz")) {
                                productFile = Zipper.decompressTarGz(archivePath,
                                                                     Paths.get(archivePath.toString().replace(".tar.gz", "")),
                                                                     true);
                            } else {
                                productFile = Zipper.decompressZip(archivePath,
                                                                   Paths.get(archivePath.toString().replace(".zip", "")),
                                                                   true);
                            }
                            if (productFile != null) {
                                try {
                                    product.setLocation(productFile.toUri().toString());
                                } catch (URISyntaxException e) {
                                    logger.severe(e.getMessage());
                                }
                            }
                        } finally {
                            if (outputStream != null && outputStream.isOpen()) outputStream.close();
                        }
                        logger.fine(String.format("End download for %s", product.getLocation()));
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

    @Override
    protected String getMetadataUrl(EOProduct descriptor) {
        throw new RuntimeException("Metadata file not supported for this strategy");
    }
}
