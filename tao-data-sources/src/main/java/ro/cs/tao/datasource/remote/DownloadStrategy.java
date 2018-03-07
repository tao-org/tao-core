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

import org.apache.http.auth.UsernamePasswordCredentials;
import ro.cs.tao.ProgressListener;
import ro.cs.tao.datasource.InterruptedException;
import ro.cs.tao.datasource.ProductFetchStrategy;
import ro.cs.tao.datasource.util.NetUtils;
import ro.cs.tao.datasource.util.Utilities;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for download fetching strategies.
 * It contains an implementation for only fetching given a product descriptor.
 *
 * @author  Cosmin Cara
 */
public abstract class DownloadStrategy implements ProductFetchStrategy {
    public static final String URL_SEPARATOR = "/";
    protected static final String NAME_SEPARATOR = "_";
    protected static final int BUFFER_SIZE = 65536;
    private static final String startMessage = "(%s,%s) %s [size: %skB]";
    private static final String completeMessage = "(%s,%s) %s [elapsed: %ss]";
    private static final String errorMessage ="Cannot download %s: %s";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    private static final String PROGRESS_KEY = "progress.enabled";
    private static final String PROGRESS_INTERVAL = "progress.interval";
    private final boolean progressEnabled;
    protected Properties props;
    protected String destination;
    protected EOProduct currentProduct;
    protected volatile double currentProductProgress;
    protected String currentStep;
    protected UsernamePasswordCredentials credentials;
    protected Set<String> filteredTiles;
    protected Pattern tileIdPattern;
    protected Logger logger = Logger.getLogger(DownloadStrategy.class.getSimpleName());
    private String localArchiveRoot;
    private FetchMode fetchMode;
    private ProgressListener progressListener;
    private volatile boolean cancelled;
    private Timer timer;
    private long progressReportInterval;

    public DownloadStrategy(String targetFolder, Properties properties) {
        this.destination = targetFolder;
        this.props = properties;
        this.progressEnabled = Boolean.parseBoolean(this.props.getProperty(PROGRESS_KEY, "true"));
        this.progressReportInterval = Long.parseLong(this.props.getProperty(PROGRESS_INTERVAL, "2000"));
    }

    public void setFilteredTiles(Set<String> tiles) {
        this.filteredTiles = tiles;
        if (tiles != null && tiles.size() > 0) {
            StringBuilder text = new StringBuilder();
            text.append("(?:.+)(");
            int idx = 1, n = tiles.size();
            for (String tile : tiles) {
                text.append(tile);
                if (idx++ < n)
                    text.append("|");
            }
            text.append(")(?:.+)");
            tileIdPattern = Pattern.compile(text.toString());
        }
    }

    @Override
    public void resume() {
        this.cancelled = false;
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
                if (!checkTileFilter(product)) {
                    return ReturnCode.EMPTY;
                }
                long startTime = System.currentTimeMillis();
                Path file = null;
                currentProduct = product;
                currentProductProgress = 0;
                try {
                    final Path destPath = Paths.get(destination);
                    FileUtils.ensureExists(destPath);
                    switch (this.fetchMode) {
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
                    logger.fine(String.format("Product download completed in %s", Utilities.formatTime(millis)));
                }
            }
        }
        return retCode;
    }

    public String getLocalArchiveRoot() {
        return localArchiveRoot;
    }

    public void setLocalArchiveRoot(String localArchiveRoot) {
        this.localArchiveRoot = localArchiveRoot;
    }

    public String getProductUrl(EOProduct descriptor){
        URI location = null;
        try {
            location = new URI(descriptor.getLocation());
        } catch (URISyntaxException ignored) {
        }
        return location != null ? location.toString() : null;
    }

    @Override
    public void setCredentials(UsernamePasswordCredentials credentials) {
        this.credentials = credentials;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setFetchMode(FetchMode mode) {
        this.fetchMode = mode;
    }

    @Override
    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    @Override
    public Path fetch(EOProduct product) throws IOException {
        activityStart(product.getName());
        try {
            currentProductProgress = 0;
            return fetchImpl(product);
        } finally {
            activityEnd();
        }
    }

    protected abstract Path fetchImpl(EOProduct product) throws IOException;

    protected boolean checkTileFilter(EOProduct product) {
        return this.filteredTiles == null || this.tileIdPattern.matcher(product.getName()).matches();
    }

    protected void checkCancelled() throws InterruptedException {
        if (cancelled) {
            throw new InterruptedException();
        }
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

    protected Path downloadFile(String remoteUrl, Path file) throws IOException, InterruptedException {
        return downloadFile(remoteUrl, file, null);
    }

    protected Path downloadFile(String remoteUrl, Path file, String authToken) throws IOException, InterruptedException {
        return downloadFile(remoteUrl, file, this.fetchMode, authToken);
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

    protected abstract String getMetadataUrl(EOProduct descriptor);

    protected boolean isCancelled() { return this.cancelled; }

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

    protected void activityStart(String activity) {
        if (this.progressListener != null) {
            this.progressListener.started(activity);
        }
        if (this.progressEnabled) {
            this.timer = new Timer("Progress reporter", true);
            this.timer.scheduleAtFixedRate(new TimedJob(), 0, this.progressReportInterval);
        }
    }

    protected void subActivityStart(String subActivity) {
        if (this.progressListener != null) {
            this.progressListener.subActivityStarted(subActivity);
        }
        if (this.progressEnabled) {
            this.timer = new Timer("Progress reporter", true);
            this.timer.scheduleAtFixedRate(new TimedJob(), 0, this.progressReportInterval);
        }
    }

    protected void activityEnd() {
        if (this.progressEnabled && timer != null) {
            this.timer.cancel();
        }
        if (this.progressListener != null) {
            this.progressListener.ended();
        }
    }

    protected void subActivityEnd(String subActivity) {
        if (this.progressEnabled && timer != null) {
            this.timer.cancel();
        }
        if (this.progressListener != null) {
            this.progressListener.subActivityEnded(subActivity);
        }
    }

    protected String getTileId(String tileName) {
        if (tileIdPattern != null) {
            Matcher matcher = tileIdPattern.matcher(tileName);
            if (matcher.matches() && matcher.groupCount() == 1) {
                // group(0) contains whole matched string and group(1) is actually the group we want
                return matcher.group(1);
            }
        }
        return "";
    }

    private Path downloadFile(String remoteUrl, Path file, FetchMode mode, String authToken) throws IOException, InterruptedException {
        checkCancelled();
        String subActivity = remoteUrl.substring(remoteUrl.lastIndexOf(URL_SEPARATOR) + 1);
        HttpURLConnection connection = null;
        try {
            logger.fine(String.format("Begin download for %s", subActivity));
            subActivityStart(subActivity);
            connection = NetUtils.openConnection(remoteUrl, authToken);
            long remoteFileLength = connection.getContentLengthLong();
            long localFileLength = 0;
            checkCancelled();
            if (Files.exists(file)) {
                localFileLength = Files.size(file);
                if (localFileLength != remoteFileLength) {
                    if (FetchMode.RESUME.equals(mode)) {
                        connection.disconnect();
                        connection = NetUtils.openConnection(remoteUrl, authToken);
                        connection.setRequestProperty("Range", "bytes=" + localFileLength + "-");
                    } else {
                        Files.delete(file);
                    }
                    logger.fine(String.format("Remote file size: %s. Local file size: %s. File " +
                                                      (FetchMode.OVERWRITE.equals(mode) ?
                                                              "will be downloaded again." :
                                                              "download will be resumed."),
                                              remoteFileLength,
                                              localFileLength));
                }
            }
            checkCancelled();
            if (localFileLength != remoteFileLength) {
                int kBytes = (int) (remoteFileLength / 1024);
                logger.fine(String.format(startMessage, currentProduct.getName(), currentStep, file.getFileName(), kBytes));
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
                    //int totalRead = 0;
                    logger.fine("Begin reading from input stream");
                    while (!cancelled && (read = inputStream.read(buffer)) != -1) {
                        outputStream.write(ByteBuffer.wrap(buffer, 0, read));
                        //totalRead += read;
                        /*if (this.progressListener != null) {
                            this.progressListener.notifyProgress(subActivity,
                                                                 (double) totalRead / (double) remoteFileLength);
                        }*/
                        currentProductProgress += currentProduct.getApproximateSize() > 0 ?
                                (double) read / (double) currentProduct.getApproximateSize() : 0;
                    }
                    logger.fine("End reading from input stream");
                    checkCancelled();
                    logger.fine(String.format(completeMessage, currentProduct.getName(), currentStep, file.getFileName(), (System.currentTimeMillis() - start) / 1000));
                } finally {
                    if (outputStream != null) outputStream.close();
                    if (inputStream != null) inputStream.close();
                }
                logger.fine(String.format("End download for %s", remoteUrl));
            } else {
                logger.fine("File already downloaded");
                logger.fine(String.format(completeMessage, currentProduct.getName(), currentStep, file.getFileName(), 0));
                currentProductProgress += Math.min(1.0, currentProduct.getApproximateSize() > 0 ?
                        (double) remoteFileLength / (double) currentProduct.getApproximateSize() : 0);
            }
        } catch (FileNotFoundException fnex) {
            logger.warning(String.format(errorMessage, remoteUrl, "No such file"));
            file = null;
        } catch (InterruptedIOException iioe) {
            logger.severe("Operation timed out");
            throw new IOException("Operation timed out");
        } catch (Exception ex) {
            logger.severe(String.format(errorMessage, remoteUrl, ex.getMessage()));
            file = null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            subActivityEnd(subActivity);
        }
        return FileUtils.ensurePermissions(file);
    }

    private class TimedJob extends TimerTask {
        @Override
        public void run() {
            if (progressListener != null) {
                progressListener.notifyProgress(currentProductProgress);
            }
        }
    }
}
