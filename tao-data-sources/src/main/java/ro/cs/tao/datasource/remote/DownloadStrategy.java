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

import org.apache.http.auth.UsernamePasswordCredentials;
import ro.cs.tao.ProgressListener;
import ro.cs.tao.datasource.InterruptedException;
import ro.cs.tao.datasource.*;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.utils.NetUtils;

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
import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
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
    protected static final int BUFFER_SIZE = 1024 * 1024;
    private static final String startMessage = "(%s,%s) %s [size: %skB]";
    private static final String completeMessage = "(%s,%s) %s [elapsed: %ss]";
    private static final String errorMessage ="Cannot download %s: %s";
    //private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    private static final String PROGRESS_KEY = "progress.enabled";
    private static final String PROGRESS_INTERVAL = "progress.interval";
    private static final String MISSING_ACTION = "local.missing.action";
    private final boolean progressEnabled;
    protected final DataSource dataSource;
    protected Properties props;
    protected String destination;
    protected EOProduct currentProduct;
    protected ProductProgress currentProductProgress;
    protected String currentStep;
    protected UsernamePasswordCredentials credentials;
    protected Set<String> filteredTiles;
    protected Pattern tileIdPattern;
    protected FetchMode fetchMode;
    protected Logger logger = Logger.getLogger(getClass().getName());
    private String localArchiveRoot;
    private ProductPathBuilder pathBuilder;
    private ProgressListener progressListener;
    private volatile boolean cancelled;
    private Timer timer;
    private long progressReportInterval;
    private boolean downloadIfNotFound;

    public DownloadStrategy(DataSource dataSource, String targetFolder, Properties properties) {
        this.dataSource = dataSource;
        this.destination = targetFolder;
        this.props = properties;
        this.progressEnabled = Boolean.parseBoolean(this.props.getProperty(PROGRESS_KEY, "true"));
        this.progressReportInterval = Long.parseLong(this.props.getProperty(PROGRESS_INTERVAL, "2000"));
        this.downloadIfNotFound = "download".equalsIgnoreCase(this.props.getProperty(MISSING_ACTION, "none"));
    }

    protected DownloadStrategy(DownloadStrategy other) {
        this.progressEnabled = other.progressEnabled;
        if (other.props != null) {
            this.props = new Properties();
            this.props.putAll(other.props);
        }
        this.destination = other.destination;
        this.credentials = other.credentials;
        if (other.filteredTiles != null) {
            this.filteredTiles = new HashSet<>();
            this.filteredTiles.addAll(other.filteredTiles);
        }
        this.tileIdPattern = other.tileIdPattern;
        this.localArchiveRoot = other.localArchiveRoot;
        this.fetchMode = other.fetchMode;
        this.progressReportInterval = other.progressReportInterval;
        this.pathBuilder = other.pathBuilder;
        this.dataSource = other.dataSource;
    }

    /**
     * Sets the tile name filter on this strategy.
     * @param tiles The tile names to be kept.
     */
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
    public void addProperties(Properties properties) {
        if (properties != null) {
            if (this.props != null) {
                this.props.putAll(properties);
            }
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

    /**
     * Returns the local arhive root (if in {@link FetchMode}.SYMLINK).
     */
    protected String getLocalArchiveRoot() {
        return localArchiveRoot;
    }

    public void setLocalArchiveRoot(String localArchiveRoot) {
        this.localArchiveRoot = localArchiveRoot;
        String format = this.props != null ? this.props.getProperty(ProductPathBuilder.LOCAL_ARCHIVE_PATH_FORMAT, "yyyy/MM/dd") : "yyyy/MM/dd";
        if (this.props != null && this.props.containsKey(ProductPathBuilder.PATH_BUILDER_CLASS)) {
            String className = this.props.getProperty(ProductPathBuilder.PATH_BUILDER_CLASS);
            try {
                Class<? extends ProductPathBuilder> clazz =
                        (Class<? extends ProductPathBuilder>) Class.forName(className);
                this.pathBuilder = clazz.getDeclaredConstructor(Path.class, String.class, Properties.class)
                                        .newInstance(Paths.get(this.localArchiveRoot), format, this.props);
            } catch (Exception e) {
                logger.severe(String.format("Cannot instantiate class '%s'. Reason: %s", className, e.getMessage()));
            }
        } else {
            this.pathBuilder = new DefaultProductPathBuilder(Paths.get(this.localArchiveRoot), format, this.props);
        }
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
        if (product == null) {
            throw new IOException("Invalid product reference [null]");
        }
        if (product.getName() == null) {
            throw new IOException("Invalid product name [null]");
        }
        activityStart(product.getName());
        Path file = null;
        try {
            currentProduct = product;
            currentProductProgress = new ProductProgress(currentProduct.getApproximateSize(), adjustProductLength());
            final Path destPath = Paths.get(destination);
            FileUtilities.ensureExists(destPath);
            switch (this.fetchMode) {
                case COPY:
                    file = copy(product, Paths.get(localArchiveRoot), destPath);
                    if (file == null) {
                        logger.warning("Product copy failed");
                    }
                    break;
                case SYMLINK:
                    if (this.filteredTiles != null || product.getAttributeValue("tiles") != null) {
                        file = link(product);
                    } else {
                        file = link(product, Paths.get(localArchiveRoot), destPath);
                    }
                    if (file == null) {
                        logger.warning("Product link failed");
                    }
                    break;
                case CHECK:
                    if (this.filteredTiles != null) {
                        file = check(product);
                    } else {
                        file = check(product, Paths.get(localArchiveRoot));
                    }
                    if (file != null) {
                        break;
                    } else {
                        logger.warning("Product check failed. Download will be attempted from the remote source");
                    }
                    break;
                case OVERWRITE:
                case RESUME:
                default:
                    file = fetchImpl(product);
                    if (file == null) {
                        logger.warning("Product download aborted");
                    }
                    break;
            }
            if (this.downloadIfNotFound && file == null &&
                this.fetchMode != FetchMode.OVERWRITE && this.fetchMode != FetchMode.RESUME) {
                file = fetchImpl(product);
                if (file == null) {
                    logger.warning("Product download aborted");
                }
            }
        } catch (IOException e) {
            logger.severe(e.getMessage());
        } finally {
            activityEnd();
        }
        return file;
    }

    public abstract DownloadStrategy clone();

    protected abstract Path fetchImpl(EOProduct product) throws IOException;

    protected abstract String getMetadataUrl(EOProduct descriptor);

    protected String getAuthenticationToken() {
        return NetUtils.getAuthToken(credentials.getUserName(), credentials.getPassword());
    }

    protected boolean checkTileFilter(EOProduct product) {
        return this.filteredTiles == null || this.tileIdPattern.matcher(product.getName()).matches();
    }

    protected void checkCancelled() throws InterruptedException {
        if (cancelled) {
            throw new InterruptedException();
        }
    }

    protected boolean adjustProductLength() {
        if (this.currentProduct != null) {
            String url = getProductUrl(this.currentProduct);
            return url.endsWith(".zip") || url.endsWith(".tar.gz");
        } else {
            return false;
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

    protected Path copyFile(Path sourcePath, Path file) throws IOException {
        return Files.exists(file) ? file : Files.copy(sourcePath, file);
    }

    protected Path downloadFile(String remoteUrl, Path file) throws IOException, InterruptedException {
        return downloadFile(remoteUrl, file, null);
    }

    protected Path downloadFile(String remoteUrl, Path file, String authToken) throws IOException, InterruptedException {
        return downloadFile(remoteUrl, file, this.fetchMode, authToken);
    }

    protected Path findProductPath(Path root, EOProduct product) {
        if (this.pathBuilder == null) {
            logger.warning("No path builder found");
            return null;
        }
        return this.pathBuilder.getProductPath(root, product);
    }

    protected boolean isCancelled() { return this.cancelled; }

    protected Path link(EOProduct product) throws IOException {
        return link(product, localArchiveRoot != null ? Paths.get(localArchiveRoot) : null, Paths.get(destination));
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

    protected Path check(EOProduct product) throws IOException {
        return check(product, Paths.get(localArchiveRoot));
    }

    protected Path check(EOProduct product, Path sourceRoot) {
        Path sourcePath = findProductPath(sourceRoot, product);
        if (sourcePath == null) {
            logger.warning(String.format("Product %s not found in the local archive", product.getName()));
        }
        return sourcePath;
    }

    protected boolean checkFile(Path file) {
        return Files.exists(file);
    }

    private void activityStart(String activity) {
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
            if (this.timer == null) {
                this.timer = new Timer("Progress reporter", true);
                this.timer.scheduleAtFixedRate(new TimedJob(), 0, this.progressReportInterval);
            }
        }
    }

    private void activityEnd() {
        if (this.progressEnabled && timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
        if (this.progressListener != null) {
            this.progressListener.ended();
        }
    }

    protected void subActivityEnd(String subActivity) {
        if (Double.compare(currentProductProgress.value(), 1.0) >= 0) {
            if (this.progressEnabled && timer != null) {
                this.timer.cancel();
                this.timer = null;
            }
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
        if ("$value".equals(subActivity)) {
            subActivity = file.getFileName().toString();
        }
        HttpURLConnection connection = null;
        try {
            logger.fine(String.format("Begin download for %s", subActivity));
            subActivityStart(subActivity);
            connection = NetUtils.openConnection(remoteUrl, authToken);
            long remoteFileLength = connection.getContentLengthLong();
            if (currentProductProgress.needsAdjustment()) {
                currentProductProgress.adjust(remoteFileLength);
            }
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
                int kBytes = (int) (remoteFileLength >> 10);
                logger.fine(String.format(startMessage, currentProduct.getName(), currentStep, file.getFileName(), kBytes));
                long start = System.currentTimeMillis();
                logger.fine(String.format("Local temporary file %s created", file.toString()));
                try (InputStream inputStream = connection.getInputStream();
                     SeekableByteChannel outputStream = Files.newByteChannel(file, EnumSet.of(StandardOpenOption.CREATE,
                                                                                              StandardOpenOption.APPEND,
                                                                                              StandardOpenOption.WRITE))) {
                    outputStream.position(localFileLength);
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int read;
                    //int totalRead = 0;
                    logger.fine("Begin reading from input stream");
                    while (!cancelled && (read = inputStream.read(buffer)) != -1) {
                        outputStream.write(ByteBuffer.wrap(buffer, 0, read));
                        currentProductProgress.add(read);
                    }
                    logger.fine("End reading from input stream");
                    checkCancelled();
                    logger.fine(String.format(completeMessage, currentProduct.getName(), currentStep, file.getFileName(), (System.currentTimeMillis() - start) / 1000));
                }
                logger.fine(String.format("End download for %s", remoteUrl));
            } else {
                logger.fine("File already downloaded");
                logger.fine(String.format(completeMessage, currentProduct.getName(), currentStep, file.getFileName(), 0));
                currentProductProgress.add(remoteFileLength);
            }
        } catch (FileNotFoundException fnex) {
            logger.warning(String.format(errorMessage, remoteUrl, "No such file"));
            file = null;
        } catch (InterruptedIOException iioe) {
            logger.severe("Operation timed out");
            throw new IOException("Operation timed out");
        } catch (Exception ex) {
            //ex.printStackTrace();
            String errMsg = String.format(errorMessage, remoteUrl, ex.getMessage());
            logger.severe(errMsg);
            throw new IOException(errMsg);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            subActivityEnd(subActivity);
        }
        return FileUtilities.ensurePermissions(file);
    }

    private class TimedJob extends TimerTask {
        private double lastValue;
        @Override
        public void run() {
            if (progressListener != null && currentProductProgress != null) {
                if (currentProductProgress.value() != lastValue) {
                    progressListener.notifyProgress(currentProductProgress.value());
                    lastValue = currentProductProgress.value();
                }
            }
        }
    }

    protected class ProductProgress {
        private double factor;
        private final DoubleAdder adder;
        private boolean needsAdjustment;

        public ProductProgress(long expectedSize, boolean needsAdjustment) {
            this.factor = expectedSize > 0 ? (double) 1 / (double) expectedSize : 0.0;
            this.adder = new DoubleAdder();
            this.needsAdjustment = needsAdjustment;
        }

        public boolean needsAdjustment() { return this.needsAdjustment; }

        public void adjust(long newSize) {
            double oldFactor = this.factor;
            this.factor = newSize > 0 ? (double) 1 / (double) newSize : 0.0;
            double oldValue = this.adder.sumThenReset();
            this.adder.add(oldValue / oldFactor * this.factor);
            this.needsAdjustment = false;
        }

        public void add(long value) {
            this.adder.add(this.factor * (double) value );
        }

        public double value() { return this.adder.doubleValue(); }
    }
}
