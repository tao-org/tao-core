package ro.cs.tao.tests;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * The BaseTest class serves as an abstract foundation for test classes, providing utility methods
 * to manage test properties and retrieve configuration values. It includes methods for setup and
 * dynamic retrieval of property values with optional prefix matching. Classes extending BaseTest
 * should use these facilities to standardize property handling across tests.
 */
public abstract class BaseTest {
    private Properties properties;
    private String prefix;

    protected void setUp() throws IOException, URISyntaxException {
        this.properties = new Properties();
        // The test properties file should be situated on the same level as 'tao-core', 'tao-plugins' and 'tao-services'
        Path path = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        for (int i = 0; i < 4; i++) {
            path = path.getParent();
        }
        path = path.resolve("test.properties");
        try (InputStream is = Files.newInputStream(path)) {
            this.properties.load(is);
        }
        prefix = getClass().getName() + ".";
    }

    protected String getValue(String key) {
        String value = null;
        if (this.properties != null) {
            value = this.properties.getProperty(prefix + key, this.properties.getProperty(key));
        }
        return value;
    }

}
