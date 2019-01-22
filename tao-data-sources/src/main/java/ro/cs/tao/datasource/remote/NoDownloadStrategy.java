package ro.cs.tao.datasource.remote;

import ro.cs.tao.datasource.InterruptedException;
import ro.cs.tao.eodata.EOProduct;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class NoDownloadStrategy extends DownloadStrategy {

    public NoDownloadStrategy(String targetFolder, Properties properties) {
        super(targetFolder, properties);
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
    protected String getMetadataUrl(EOProduct descriptor) {
        throw new RuntimeException("Metadata file not supported for this strategy");
    }
}
