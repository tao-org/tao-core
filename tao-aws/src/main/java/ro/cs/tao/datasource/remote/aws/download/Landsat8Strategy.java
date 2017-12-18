package ro.cs.tao.datasource.remote.aws.download;

import ro.cs.tao.datasource.remote.DownloadStrategy;
import ro.cs.tao.datasource.remote.aws.AWSDataSource;
import ro.cs.tao.datasource.remote.aws.helpers.LandsatProductHelper;
import ro.cs.tao.datasource.util.Utilities;
import ro.cs.tao.eodata.EOProduct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public class Landsat8Strategy extends DownloadStrategy {
    private static final Properties properties;
    private static final Set<String> bandFiles = new LinkedHashSet<String>() {{
        add("_B1.TIF");
        add("_B2.TIF");
        add("_B3.TIF");
        add("_B4.TIF");
        add("_B5.TIF");
        add("_B6.TIF");
        add("_B7.TIF");
        add("_B8.TIF");
        add("_B9.TIF");
        add("_B10.TIF");
        add("_B11.TIF");
        add("_BQA.TIF");
    }};

    private String baseUrl;

    static {
        properties = new Properties();
        try {
            properties.load(AWSDataSource.class.getResourceAsStream("aws.properties"));
        } catch (IOException ignored) {
        }
    }

    public Landsat8Strategy(String targetFolder) {
        super(targetFolder, properties);
        baseUrl = props.getProperty("l8.aws.products.url", "http://landsat-pds.s3.amazonaws.com/");
        if (!baseUrl.endsWith("/"))
            baseUrl += "/";
    }

    @Override
    public Path fetch(String productName) throws IOException {
        return fetch(new EOProduct() {{ setName(productName); }});
    }

    @Override
    public Path fetch(EOProduct product) throws IOException {
        String url;
        currentProduct = product;
        String productName = currentProduct.getName();
        LandsatProductHelper helper = new LandsatProductHelper(productName);
        Path rootPath = Utilities.ensureExists(Paths.get(destination, productName));
        url = getMetadataUrl(currentProduct);
        Path metadataFile = rootPath.resolve(productName + "_MTL.txt");
        currentStep = "Metadata";
        getLogger().fine(String.format("Downloading metadata file %s", metadataFile));
        metadataFile = downloadFile(url, metadataFile);
        if (metadataFile != null && Files.exists(metadataFile)) {
            for (String suffix : bandFiles) {
                String bandFileName = productName + suffix;
                currentStep = "Band " + bandFileName;
                try {
                    String bandFileUrl = getProductUrl(currentProduct) + URL_SEPARATOR + bandFileName;
                    Path path = rootPath.resolve(bandFileName);
                    getLogger().fine(String.format("Downloading band raster %s from %s", path, bandFileUrl));
                    downloadFile(bandFileUrl, path);
                } catch (IOException ex) {
                    getLogger().warning(String.format("Download for %s failed [%s]", bandFileName, ex.getMessage()));
                }
            }
            if ("coll".equals(helper.getVersion())) {
                String fileName = productName + "_ANG.txt";
                try {
                    String fileUrl = getProductUrl(currentProduct) + URL_SEPARATOR + fileName;
                    Path path = rootPath.resolve(fileName);
                    getLogger().fine(String.format("Downloading band raster %s from %s", path, fileUrl));
                    downloadFile(fileUrl, path);
                } catch (IOException ex) {
                    getLogger().warning(String.format("Download for %s failed [%s]", fileName, ex.getMessage()));
                }
            }
        } else {
            getLogger().warning(
                    String.format("Either the product %s was not found or the metadata file could not be downloaded",
                                  productName));
            rootPath = null;
        }
        return rootPath;
    }

    @Override
    public String getProductUrl(EOProduct descriptor) {
        String productUrl = super.getProductUrl(descriptor);
        if (productUrl == null) {
            productUrl = baseUrl + new LandsatProductHelper(descriptor.getName()).getProductRelativePath();
        }
        return productUrl;
    }

    @Override
    protected String getMetadataUrl(EOProduct descriptor) {
        return getProductUrl(descriptor) + DownloadStrategy.URL_SEPARATOR + descriptor.getName() + "_MTL.txt";
    }

    private Logger getLogger() { return Logger.getLogger(Sentinel2Strategy.class.getSimpleName()); }
}
