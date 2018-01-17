package ro.cs.tao.datasource.remote.aws.download;

import ro.cs.tao.datasource.remote.DownloadStrategy;
import ro.cs.tao.datasource.remote.aws.AWSDataSource;
import ro.cs.tao.datasource.remote.aws.helpers.Sentinel2ProductHelper;
import ro.cs.tao.datasource.util.NetUtils;
import ro.cs.tao.datasource.util.Utilities;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.utils.FileUtils;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author Cosmin Cara
 */
public class Sentinel2Strategy extends DownloadStrategy {
    private static final Properties properties;
    private static final Set<String> l1cBandFiles;

    private static final String FOLDER_GRANULE = "GRANULE";
    private static final String FOLDER_AUXDATA = "AUX_DATA";
    private static final String FOLDER_DATASTRIP = "DATASTRIP";
    private static final String FOLDER_IMG_DATA = "IMG_DATA";
    private static final String FOLDER_QI_DATA = "QI_DATA";

    private String productsUrl;
    private String baseUrl;

    private Set<String> filteredTiles;
    private boolean shouldFilterTiles;
    private Pattern tileIdPattern;

    static {
        properties = new Properties();
        try {
            properties.load(AWSDataSource.class.getResourceAsStream("aws.properties"));
        } catch (IOException ignored) {
        }
        l1cBandFiles = new LinkedHashSet<String>() {{
            add("B01.jp2");
            add("B02.jp2");
            add("B03.jp2");
            add("B04.jp2");
            add("B05.jp2");
            add("B06.jp2");
            add("B07.jp2");
            add("B08.jp2");
            add("B8A.jp2");
            add("B09.jp2");
            add("B10.jp2");
            add("B11.jp2");
            add("B12.jp2");
        }};
    }

    public Sentinel2Strategy(String targetFolder) {
        super(targetFolder, properties);
        baseUrl = props.getProperty("s2.aws.tiles.url", "http://sentinel-s2-l1c.s3-website.eu-central-1.amazonaws.com");
        if (!baseUrl.endsWith("/"))
            baseUrl += "/";
        productsUrl = baseUrl + "products/";
    }

    public void setFilteredTiles(Set<String> tiles) {
        this.filteredTiles = tiles;
        if (shouldFilterTiles = (tiles != null && tiles.size() > 0)) {
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
    protected String getMetadataUrl(EOProduct product) {
        return getProductUrl(product) + "metadata.xml";
    }

    @Override
    public String getProductUrl(EOProduct descriptor) {
        String url = super.getProductUrl(descriptor);
        if (url == null || !url.startsWith(baseUrl)) {
            url = productsUrl + Sentinel2ProductHelper.createHelper(descriptor.getName()).getProductRelativePath();
        }
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url;
    }

    @Override
    public Path fetch(String productName) throws IOException {
        return fetch(new EOProduct() {{ setName(productName); }});
    }

    @Override
    public Path fetch(EOProduct product) throws IOException {
        return downloadImpl(product);
    }

    private Path downloadImpl(EOProduct product) throws IOException {
        Path rootPath = null;
        String url;
        currentProduct = product;
        currentProductProgress = 0;
        FileUtils.ensureExists(Paths.get(destination));
        String productName = product.getName();
        Sentinel2ProductHelper helper = Sentinel2ProductHelper.createHelper(productName);
        // let's try to assemble the product
        rootPath = FileUtils.ensureExists(Paths.get(destination, productName + ".SAFE"));
        String baseProductUrl = getProductUrl(product);
        url = getMetadataUrl(product);
        Path metadataFile = rootPath.resolve(helper.getMetadataFileName());
        currentStep = "Metadata";
        getLogger().fine(String.format("Downloading metadata file %s", metadataFile));
        metadataFile = downloadFile(url, metadataFile);
        if (metadataFile != null && Files.exists(metadataFile)) {
            Path inspireFile = metadataFile.resolveSibling("INSPIRE.xml");
            Path manifestFile = metadataFile.resolveSibling("manifest.safe");
            Path previewFile = metadataFile.resolveSibling("preview.png");
            List<String> allLines = Files.readAllLines(metadataFile);
            List<String> metaTileNames = Utilities.filter(allLines, "<Granule" + ("13".equals(helper.getVersion()) ? "s" : " "));

            boolean hasTiles = updateMetadata(metadataFile, allLines);
            if (hasTiles) {
                downloadFile(baseProductUrl + "inspire.xml", inspireFile);
                downloadFile(baseProductUrl + "manifest.safe", manifestFile);
                downloadFile(baseProductUrl + "preview.png", previewFile);

                // rep_info folder and contents
                Path repFolder = FileUtils.ensureExists(rootPath.resolve("rep_info"));
                Path schemaFile = repFolder.resolve("S2_User_Product_Level-1C_Metadata.xsd");
                copyFromResources(String.format("S2_User_Product_Level-1C_Metadata%s.xsd", helper.getVersion()), schemaFile);
                // HTML folder and contents
                Path htmlFolder = FileUtils.ensureExists(rootPath.resolve("HTML"));
                copyFromResources("banner_1.png", htmlFolder);
                copyFromResources("banner_2.png", htmlFolder);
                copyFromResources("banner_3.png", htmlFolder);
                copyFromResources("star_bg.jpg", htmlFolder);
                copyFromResources("UserProduct_index.html", htmlFolder);
                copyFromResources("UserProduct_index.xsl", htmlFolder);

                Path tilesFolder = FileUtils.ensureExists(rootPath.resolve(FOLDER_GRANULE));
                FileUtils.ensureExists(rootPath.resolve(FOLDER_AUXDATA));
                Path dataStripFolder = FileUtils.ensureExists(rootPath.resolve(FOLDER_DATASTRIP));
                String productJsonUrl = baseProductUrl + "productInfo.json";
                HttpURLConnection connection = null;
                InputStream inputStream = null;
                JsonReader reader = null;
                try {
                    getLogger().fine(String.format("Downloading json product descriptor %s", productJsonUrl));
                    connection = NetUtils.openConnection(productJsonUrl);
                    inputStream = connection.getInputStream();
                    reader = Json.createReader(inputStream);
                    getLogger().fine(String.format("Parsing json descriptor %s", productJsonUrl));
                    JsonObject obj = reader.readObject();
                    final Map<String, String> tileNames = getTileNames(obj, metaTileNames, helper.getVersion());
                    String dataStripId = null;
                    String count = String.valueOf(tileNames.size());
                    int tileCounter = 1;
                    for (Map.Entry<String, String> entry : tileNames.entrySet()) {
                        currentStep = "Tile " + String.valueOf(tileCounter++) + "/" + count;
                        String tileUrl = entry.getValue();
                        String tileName = entry.getKey();
                        Path tileFolder = FileUtils.ensureExists(tilesFolder.resolve(tileName));
                        Path auxData = FileUtils.ensureExists(tileFolder.resolve(FOLDER_AUXDATA));
                        Path imgData = FileUtils.ensureExists(tileFolder.resolve(FOLDER_IMG_DATA));
                        Path qiData = FileUtils.ensureExists(tileFolder.resolve(FOLDER_QI_DATA));
                        String metadataName = helper.getGranuleMetadataFileName(tileName);
                        getLogger().fine(String.format("Downloading tile metadata %s", tileFolder.resolve(metadataName)));
                        downloadFile(tileUrl + "/metadata.xml", tileFolder.resolve(metadataName));
                        List<String> tileMetadataLines = Files.readAllLines(metadataFile);
                        for (String bandFileName : l1cBandFiles) {
                            try {
                                String bandFileUrl = tileUrl + URL_SEPARATOR + bandFileName;
                                Path path = imgData.resolve(helper.getBandFileName(tileName, bandFileName));
                                getLogger().fine(String.format("Downloading band raster %s from %s", path, bandFileName));
                                downloadFile(bandFileUrl, path);
                            } catch (IOException ex) {
                                getLogger().warning(String.format("Download for %s failed [%s]", bandFileName, ex.getMessage()));
                            }
                        }
                        List<String> lines = Utilities.filter(tileMetadataLines, "<MASK_FILENAME");
                        for (String line : lines) {
                            line = line.trim();
                            int firstTagCloseIdx = line.indexOf(">") + 1;
                            int secondTagBeginIdx = line.indexOf("<", firstTagCloseIdx);
                            String maskFileName = line.substring(firstTagCloseIdx, secondTagBeginIdx);
                            String remoteName;
                            Path path;
                            if ("13".equals(helper.getVersion())) {
                                String[] tokens = maskFileName.split(NAME_SEPARATOR);
                                remoteName = tokens[2] + NAME_SEPARATOR + tokens[3] + NAME_SEPARATOR + tokens[9] + ".gml";
                                path = qiData.resolve(maskFileName);
                            } else {
                                remoteName = maskFileName.substring(maskFileName.lastIndexOf(URL_SEPARATOR) + 1);
                                path = rootPath.resolve(maskFileName);
                            }

                            try {
                                String fileUrl = tileUrl + "/qi/" + remoteName;
                                getLogger().fine(String.format("Downloading file %s from %s", path, fileUrl));
                                downloadFile(fileUrl, path);
                            } catch (IOException ex) {
                                getLogger().warning(String.format("Download for %s failed [%s]", path, ex.getMessage()));
                            }
                        }
                        getLogger().fine(String.format("Trying to download %s", tileUrl + "/auxiliary/ECMWFT"));
                        downloadFile(tileUrl + "/auxiliary/ECMWFT", auxData.resolve(helper.getEcmWftFileName(tileName)));
                        if (dataStripId == null) {
                            String tileJson = tileUrl + "/tileInfo.json";
                            HttpURLConnection tileConnection = null;
                            InputStream is = null;
                            JsonReader tiReader = null;
                            try {
                                getLogger().fine(String.format("Downloading json tile descriptor %s", tileJson));
                                tileConnection = NetUtils.openConnection(tileJson);
                                is = tileConnection.getInputStream();
                                tiReader = Json.createReader(is);
                                getLogger().fine(String.format("Parsing json tile descriptor %s", tileJson));
                                JsonObject tileObj = tiReader.readObject();
                                dataStripId = tileObj.getJsonObject("datastrip").getString("id");
                                String dataStripPath = tileObj.getJsonObject("datastrip").getString("path") + "/metadata.xml";
                                Path dataStrip = FileUtils.ensureExists(dataStripFolder.resolve(helper.getDatastripFolder(dataStripId)));
                                String dataStripFile = helper.getDatastripMetadataFileName(dataStripId);
                                FileUtils.ensureExists(dataStrip.resolve(FOLDER_QI_DATA));
                                getLogger().fine(String.format("Downloading %s", baseUrl + dataStripPath));
                                downloadFile(baseUrl + dataStripPath, dataStrip.resolve(dataStripFile));
                            } finally {
                                if (tiReader != null) tiReader.close();
                                if (is != null) is.close();
                                if (tileConnection != null) tileConnection.disconnect();
                            }
                        }
                    }
                } finally {
                    if (reader != null) reader.close();
                    if (inputStream != null) inputStream.close();
                    if (connection != null) connection.disconnect();
                }
            } else {
                Files.deleteIfExists(metadataFile);
                rootPath = null;
                getLogger().warning(String.format("The product %s did not contain any tiles from the tile list", productName));
            }
        } else {
            getLogger().warning(String.format("Either the product %s was not found in the data bucket or the metadata file could not be downloaded", productName));
            rootPath = null;
        }
        return rootPath;
    }

    private Map<String, String> getTileNames(JsonObject productInfo, List<String> metaTileNames, String psdVersion) {
        Map<String, String> ret = new HashMap<>();
        JsonArray tiles = productInfo.getJsonArray("tiles");
        for (JsonObject result : tiles.getValuesAs(JsonObject.class)) {
            String tilePath = result.getString("path");
            String[] tokens = tilePath.split(URL_SEPARATOR);
            String tileId = "T" + tokens[1] + tokens[2] + tokens[3];
            String tileName = Utilities.find(metaTileNames, tileId, psdVersion);
            ret.put(tileName, baseUrl + tilePath);
        }
        return ret;
    }

    private void copyFromResources(String fileName, Path file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName)))) {
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            if (Files.isDirectory(file)) {
                FileUtils.ensurePermissions(Files.write(file.resolve(fileName), builder.toString().getBytes()));
            } else {
                FileUtils.ensurePermissions(Files.write(file, builder.toString().getBytes()));
            }
        }
    }

    private boolean updateMetadata(Path metaFile, List<String> originalLines) throws IOException {
        boolean canProceed = true;
        if (shouldFilterTiles) {
            int tileCount = 0;
            List<String> lines = new ArrayList<>();
            for (int i = 0; i < originalLines.size(); i++) {
                String line = originalLines.get(i);
                if (line.contains("<Granule_List>")) {
                    if (tileIdPattern.matcher(originalLines.get(i + 1)).matches()) {
                        lines.addAll(originalLines.subList(i, i + 17));
                        tileCount++;
                    }
                    i += 16;
                } else {
                    lines.add(line);
                }
            }
            if (canProceed = (tileCount > 0)) {
                Files.write(metaFile, lines, StandardCharsets.UTF_8);
            }
        }
        return canProceed;
    }

    private Logger getLogger() { return Logger.getLogger(Sentinel2Strategy.class.getSimpleName()); }
}
