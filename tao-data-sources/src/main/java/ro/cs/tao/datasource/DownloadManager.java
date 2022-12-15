package ro.cs.tao.datasource;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.utils.executors.NamedThreadPoolExecutor;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Utility class for allowing limiting the number of concurrent downloads per data source, as specified
 * by the data source implementation.
 * This class is used only if downloads are done via {@see DataSourceComponent} class.
 *
 * @author Cosmin Cara
 */
public class DownloadManager {
    private static final String SERIALIZE_DOWNLOADS_KEY = "serialize.downloads";
    private static final DownloadManager instance;
    private static final Logger logger;
    private static final boolean serializeDownloads = ConfigurationManager.getInstance().getBooleanValue(SERIALIZE_DOWNLOADS_KEY);
    private final Map<String, NamedThreadPoolExecutor> downloadWorkers;
    private final Map<Integer, Future<?>> queuedDownloads;

    static {
        instance = new DownloadManager();
        logger = Logger.getLogger(DownloadManager.class.getName());
        for (DataSource<?, ?> dataSource : DataSourceManager.getInstance().getRegisteredDataSources()) {
            initializeDatasource(dataSource);
        }
    }

    /**
     * Sets the maximum allowed concurrent downloads for the given data source.
     * If the given value is greater than the value allowed for the data source, an exception is thrown.
     *
     * @param dataSourceName    The data source name.
     * @param value             The new value for maximum concurrent downloads.
     */
    public static void setConcurrentDownloads(String dataSourceName, int value) {
        final NamedThreadPoolExecutor poolExecutor = instance.downloadWorkers.get(dataSourceName);
        final DataSource<?, ?> dataSource = DataSourceManager.getInstance().getRegisteredDataSources().stream().filter(d -> d.getId().equals(dataSourceName)).findFirst().orElse(null);
        if (dataSource == null) {
            throw new IllegalArgumentException(String.format("[%s] not registered", dataSourceName));
        }
        final int maxAllowed = dataSource.getMaximumAllowedTransfers();
        if (value > maxAllowed) {
            throw new IllegalArgumentException(String.format("[%s] shall allow maximum %d concurrent downloads", dataSourceName, maxAllowed));
        }
        if (poolExecutor != null) {
            final int initialSize = poolExecutor.getCorePoolSize();
            poolExecutor.setCorePoolSize(value);
            poolExecutor.setMaximumPoolSize(value);
            logger.fine(String.format("Data source [%s] updated from %d to %d concurrent downloads",
                                      dataSourceName, initialSize, value));
        } else {
            instance.downloadWorkers.put(dataSourceName, new NamedThreadPoolExecutor(dataSourceName, value));
        }
    }

    /**
     * Queues a download from the given {@see DataSourceComponent} instance. If the number of concurrent downloads reached
     * the maximum set value, the request will be queued and processed when one of the current downloads completes.
     * The method blocks until the download completes, but doesn't prevent receiving other requests from other components.
     *
     * @param dsInstance            The DataSourceComponent instance
     * @param delegate              The delegate to the actual download method of the component instance
     * @param products              The list of products to be fetched
     * @param tiles                 The tile filter
     * @param destinationPath       The location where the products will be placed
     * @param localRootPath         The path to the local products archive
     * @param additionalProperties  Additional properties that can be passed to the component
     */
    public static List<EOProduct> queueDownload(DataSourceComponent dsInstance,
                                                FiveArgsFunction<List<EOProduct>, Set<String>, String, String, Properties, List<EOProduct>> delegate,
                                                List<EOProduct> products, Set<String> tiles, String destinationPath, String localRootPath, Properties additionalProperties) {
        try {
            final String dataSourceName = dsInstance.getDataSourceName();
            final NamedThreadPoolExecutor executor = instance.downloadWorkers.get(dataSourceName);
            if (executor == null) {
                initializeDatasource(DataSourceManager.getInstance().getRegisteredDataSources()
                                                      .stream().filter(d -> d.getId().equals(dataSourceName)).findFirst().get());
                //throw new Exception(String.format("No download worker for source %s found", dsInstance.getDataSourceName()));
            }
            final Future<List<EOProduct>> task = executor.submit(() ->
                         delegate.apply(products, tiles, destinationPath, localRootPath, additionalProperties));
            try {
                instance.queuedDownloads.put(dsInstance.hashCode(), task);
                logger.fine(String.format("Download queued for data source [%s-%s]. There are %d queued downloads for this source",
                                          dataSourceName, dsInstance.getSensorName(), executor.getActiveCount()));
                return task.get();
            } finally {
                instance.queuedDownloads.remove(dsInstance.hashCode());
                logger.fine(String.format("One download completed for data source [%s-%s]. There are %d queued downloads for this source",
                                          dataSourceName, dsInstance.getSensorName(), executor.getActiveCount()));
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
            return null;
        }
    }

    /**
     * Cancels any existing download for the given component instance, or removes a pending download from the queue.
     *
     * @param dsInstance        The DataSourceComponent instance
     */
    public static void cancelDownload(DataSourceComponent dsInstance, Callable<Void> delegate) {
        try {
            delegate.call();
        } catch (Exception ignored) {
        }
        final NamedThreadPoolExecutor executor = instance.downloadWorkers.get(dsInstance.getId());
        if (executor != null) {
            final Future<?> task = instance.queuedDownloads.remove(dsInstance.hashCode());
            if (task != null) {
                task.cancel(true);
            } else {
                logger.warning(String.format("No download was queued for data source [%s-%s@%d]",
                                             dsInstance.getDataSourceName(), dsInstance.getSensorName(), dsInstance.hashCode()));
            }
        }
    }

    /**
     * Returns the statistics of the manager (per data source, how many downloads are in progress, how many are queued).
     *
     */
    public static Map<String, String> getCurrentStatus() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, NamedThreadPoolExecutor> entry : instance.downloadWorkers.entrySet()) {
            result.put(entry.getKey(),
                       String.format("%d active, %d queued",
                                     entry.getValue().getActiveCount(), entry.getValue().getQueue().size()));
        }
        return result;
    }

    private static void initializeDatasource(DataSource<?, ?> dataSource) {
        String dsName = dataSource.getId();
        int maxTransfers;
        if (serializeDownloads) {
            maxTransfers = 1;
            setConcurrentDownloads(dsName, maxTransfers);
        } else {
            maxTransfers = dataSource.getMaximumAllowedTransfers();
            instance.downloadWorkers.put(dsName, new NamedThreadPoolExecutor(dsName, maxTransfers));
        }
        logger.fine(String.format("Data source [%s] initialized with %d concurrent downloads", dsName, maxTransfers));
    }

    private DownloadManager() {
        this.downloadWorkers = Collections.synchronizedMap(new HashMap<>());
        this.queuedDownloads = Collections.synchronizedMap(new HashMap<>());
    }
}
