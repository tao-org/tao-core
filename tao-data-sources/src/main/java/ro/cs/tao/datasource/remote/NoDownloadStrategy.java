package ro.cs.tao.datasource.remote;

import ro.cs.tao.datasource.DataSource;
import ro.cs.tao.datasource.InterruptedException;
import ro.cs.tao.eodata.EOProduct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class NoDownloadStrategy extends DownloadStrategy {

    public NoDownloadStrategy(DataSource dataSource, String targetFolder, Properties properties) {
        super(dataSource, targetFolder, properties);
    }

    private NoDownloadStrategy(NoDownloadStrategy other) {
        super(other);
    }

    @Override
    public NoDownloadStrategy clone() {
        return new NoDownloadStrategy(this);
    }

    @Override
    protected Path fetchImpl(EOProduct product) throws InterruptedException {
        checkCancelled();
        if (currentProduct == null) {
            currentProduct = product;
        }
        return Paths.get(product.getLocation());
    }

    @Override
    protected Path check(EOProduct product) throws IOException {
        checkCancelled();
        final Path path = Paths.get(product.getLocation());
        if (!Files.exists(path)) {
            throw new IOException(String.format("Product %s not found in the local archive", product.getName()));
        }
        return path;
    }

    @Override
    protected Path check(EOProduct product, Path sourceRoot) throws IOException {
        return check(product);
    }

    @Override
    protected String getMetadataUrl(EOProduct descriptor) {
        throw new RuntimeException("Metadata file not supported for this strategy");
    }
}
