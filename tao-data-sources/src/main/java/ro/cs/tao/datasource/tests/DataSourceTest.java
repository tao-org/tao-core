package ro.cs.tao.datasource.tests;

import org.junit.Assert;
import org.junit.Before;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSource;
import ro.cs.tao.datasource.ProductFetchStrategy;
import ro.cs.tao.datasource.param.CommonParameterNames;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.tests.BaseTest;
import ro.cs.tao.utils.DateUtils;
import ro.cs.tao.utils.executors.monitoring.DownloadProgressListener;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public abstract class DataSourceTest<T extends DataSource> extends BaseTest {
    protected static final int pageSize;
    protected static final int maxResults;
    protected static final String rowTemplate;
    protected static final DownloadProgressListener listener;

    protected final Class<T> dsClass;
    protected QueryParameter<LocalDateTime> startDate;
    protected QueryParameter<LocalDateTime> endDate;
    protected QueryParameter<Polygon2D> aoi;
    protected String user = "";
    protected String password = "";
    protected Path destination;

    static {
        pageSize = 100;
        maxResults = 10000;
        rowTemplate = "ID=%s, NAME=%s, LOCATION=%s, QUICKLOOK=%s";
        listener = new DownloadProgressListener() {
            @Override
            public void started(String taskName) {
                System.out.println("Started " + taskName);
            }

            @Override
            public void subActivityStarted(String subTaskName) {
                System.out.println("Started " + subTaskName);
            }

            @Override
            public void subActivityEnded(String subTaskName) {
                System.out.println("Finished " + subTaskName);
            }

            @Override
            public void ended() {
                System.out.println("Download completed");
            }

            @Override
            public void notifyProgress(double progressValue) {
                System.out.printf("Progress: %.2f%%\r", progressValue * 100);
            }

            @Override
            public void notifyProgress(double progressValue, double transferSpeed) {
                System.out.printf("Progress: %.2f%% [%.2fMB/s]\r", progressValue * 100, transferSpeed);
            }

            @Override
            public void notifyProgress(String subTaskName, double subTaskProgress) {
                System.out.printf("Progress: %s %.2f%%\n", subTaskName, subTaskProgress * 100);
            }

            @Override
            public void notifyProgress(String subTaskName, double subTaskProgress, double overallProgress) {
                System.out.printf("Progress: %s %.2f%% (%.2f%%)\n", subTaskName, subTaskProgress * 100, overallProgress * 100);
            }
        };
    }

    public DataSourceTest(Class<T> dsClass) {
        this.dsClass = dsClass;
    }

    @Before
    public void setUp() throws IOException, URISyntaxException {
        super.setUp();
        destination = Files.createTempDirectory("tmp");
        startDate = new QueryParameter<>(LocalDateTime.class, CommonParameterNames.START_DATE);
        startDate.setValue(DateUtils.parseDateTime(getValue(CommonParameterNames.START_DATE)));
        endDate = new QueryParameter<>(LocalDateTime.class, CommonParameterNames.END_DATE);
        endDate.setValue(DateUtils.parseDateTime(getValue(CommonParameterNames.END_DATE)));
        Polygon2D footprint = Polygon2D.fromWKT(getValue(CommonParameterNames.FOOTPRINT));
        aoi = new QueryParameter<>(Polygon2D.class, CommonParameterNames.FOOTPRINT);
        aoi.setValue(footprint);
        user = getValue("user");
        password = getValue("password");
        Logger logger = LogManager.getLogManager().getLogger("");
        for (Handler handler : logger.getHandlers()) {
            handler.setLevel(Level.FINEST);
        }
    }

    protected T getInstance() {
        try {
            return this.dsClass.getConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    protected void testSensor(String sensor, String productType, boolean download, QueryParameter<?>...additionalParams) {
            try {
                DataSource<?, ?> dataSource = getInstance();
                dataSource.setCredentials(user, password);

                DataQuery query = dataSource.createQuery(sensor);
                query.addParameter(startDate);
                query.addParameter(endDate);
                query.addParameter(aoi);
                query.addParameter(CommonParameterNames.PRODUCT_TYPE, productType);
                if (additionalParams != null) {
                    for (QueryParameter<?> parameter : additionalParams) {
                        query.addParameter(parameter);
                    }
                }
                query.setMaxResults(maxResults);
                List<EOProduct> results = query.execute();
                testAssertions(results);
                if (download) {
                    final ProductFetchStrategy strategy = dataSource.getProductFetchStrategy(sensor);
                    if (!results.isEmpty()) {
                        //strategy.setFetchMode(FetchMode.OVERWRITE);
                        //strategy.setDestination(destination);
                        strategy.setProgressListener(listener);
                        Path path = strategy.fetch(results.get(0));
                        strategy.cancel();
                        if (path != null) {
                            System.out.println("Product downloaded at " + path.toString());
                        } else {
                            System.out.println("Product not downloaded");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    protected void testAssertions(List<EOProduct> results) {
        Assert.assertNotNull(results);
        Assert.assertNotEquals(0, results.size());
        Assert.assertNotNull(results.get(0));
        Assert.assertNotNull(results.get(0).getId());
        Assert.assertNotNull(results.get(0).getLocation());
    }

}
