package ro.cs.tao.datasource;

import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Cosmin Cara
 */
public class DataSourceManager {

    private static final DataSourceManager instance;
    private final ServiceRegistry<DataSource> registry;
    private final Map<String, List<String>> registeredSources;

    static {
        instance = new DataSourceManager();
    }

    public static void main(String[] args) {
        System.out.println("OK!");
    }

    public static DataSourceManager getInstance() { return instance; }

    private DataSourceManager() {
        this.registry = ServiceRegistryManager.getInstance().getServiceRegistry(DataSource.class);
        this.registeredSources = new HashMap<>();
        final Set<DataSource> services = this.registry.getServices();
        services.forEach(ds -> {
            final String[] sensors = ds.getSupportedSensors();
            final String className = ds.getClass().getName();
            for (String sensor : sensors) {
                if (!this.registeredSources.containsKey(sensor)) {
                    this.registeredSources.put(sensor, new ArrayList<>());
                }
                this.registeredSources.get(sensor).add(className);
            }
        });
    }

    /**
     * Returns the name of the first data source associated with the given sensor,
     * or <code>null</code> if no data source is associated.
     *
     * @param sensorName    The sensor name
     */
    public String getFirst(String sensorName) {
        List<String> found = this.registeredSources.get(sensorName);
        return found != null ? found.get(0) : null;
    }

    /**
     * Returns the names of the data sources associated with the given sensor,
     * or an empty list if no data source is associated.
     *
     * @param sensorName    The sensor name
     */
    public List<String> getNames(String sensorName) {
        List<String> names = new ArrayList<>();
        List<String> found = this.registeredSources.get(sensorName);
        if (found != null) {
            names.addAll(found);
        }
        return names;
    }

    /**
     * Returns an instance of the first data source associated with the given sensor,
     * or null if no data source is associated.
     *
     * @param sensorName    The name of the sensor
     */
    public DataSource get(String sensorName) {
        DataSource dataSource = null;
        List<String> sourceNames = this.registeredSources.get(sensorName);
        if (sourceNames != null) {
            dataSource = this.registry.getService(sourceNames.get(0));
        }
        return dataSource;
    }

    /**
     * Returns an instance of the given data source for the given sensor, if the data source
     * is registered.
     *
     * @param sensorName    The sensor name
     * @param className     The data source class name
     */
    public DataSource get(String sensorName, String className) {
        DataSource dataSource = null;
        List<String> sourceNames = this.registeredSources.get(sensorName);
        if (sourceNames != null && sourceNames.stream().anyMatch(s -> s.equals(className))) {
            dataSource = this.registry.getService(className);
        }
        return dataSource;
    }
}
