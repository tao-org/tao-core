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

package ro.cs.tao.datasource.remote.scihub;

import ro.cs.tao.datasource.remote.scihub.helpers.Sentinel2ProductHelper;
import ro.cs.tao.datasource.util.NetUtils;
import ro.cs.tao.datasource.util.Utilities;
import ro.cs.tao.eodata.EOData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Cosmin Cara
 */
public class Sentinel2Downloader extends SentinelDownloader {
    private static final Set<String> l1cBandFiles = new LinkedHashSet<String>() {{
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
    private static final Map<String, Set<String>> l2aBandFiles = new HashMap<String, Set<String>>() {{
        Set<String> files = new LinkedHashSet<>();
        put("R10m", files);
        files.add("AOT_10m.jp2");
        files.add("B02_10m.jp2");
        files.add("B03_10m.jp2");
        files.add("B04_10m.jp2");
        files.add("B08_10m.jp2");
        files.add("TCI_10m.jp2");
        files.add("WVP_10m.jp2");
        files = new LinkedHashSet<>();
        put("R20m", files);
        files.add("AOT_20m.jp2");
        files.add("B02_20m.jp2");
        files.add("B03_20m.jp2");
        files.add("B04_20m.jp2");
        files.add("B05_20m.jp2");
        files.add("B06_20m.jp2");
        files.add("B07_20m.jp2");
        files.add("B11_20m.jp2");
        files.add("B12_20m.jp2");
        files.add("B8A_20m.jp2");
        files.add("SCL_20m.jp2");
        files.add("TCI_20m.jp2");
        files.add("VIS_20m.jp2");
        files.add("WVP_20m.jp2");
        files = new LinkedHashSet<>();
        put("R60m", files);
        files.add("AOT_60m.jp2");
        files.add("B01_60m.jp2");
        files.add("B02_60m.jp2");
        files.add("B03_60m.jp2");
        files.add("B04_60m.jp2");
        files.add("B05_60m.jp2");
        files.add("B06_60m.jp2");
        files.add("B07_60m.jp2");
        files.add("B09_60m.jp2");
        files.add("B11_60m.jp2");
        files.add("B12_60m.jp2");
        files.add("B8A_60m.jp2");
        files.add("SCL_60m.jp2");
        files.add("TCI_60m.jp2");
        files.add("WVP_60m.jp2");
    }};
    private static final Set<String> l2aMasks = new LinkedHashSet<String>() {{
        add("CLD_20m.jp2");
        add("CLD_60m.jp2");
        add("PVI.jp2");
        add("SNW_20m.jp2");
        add("SNW_60m.jp2");
    }};

    private static final String FOLDER_GRANULE = "GRANULE";
    private static final String FOLDER_AUXDATA = "AUX_DATA";
    private static final String FOLDER_DATASTRIP = "DATASTRIP";
    private static final String FOLDER_IMG_DATA = "IMG_DATA";
    private static final String FOLDER_QI_DATA = "QI_DATA";
    private static final String XML_ATTR_GRANULE_ID = "granuleIdentifier";
    private static final String XML_ATTR_DATASTRIP_ID = "datastripIdentifier";

    private String odataProductPath;
    private String odataTilePath;
    private String odataMetadataPath;

    private Set<String> filteredTiles;
    private boolean shouldFilterTiles;
    private Pattern tileIdPattern;

    public Sentinel2Downloader(String targetFolder) {
        super(targetFolder);
        ODataPath odp = new ODataPath();
        odataProductPath = odp.root(oDataBasePath).node("${PRODUCT_NAME}.SAFE").path();
        odp.root(odataProductPath).node(FOLDER_GRANULE).node("${tile}");
        odataTilePath = odp.path();
        odataMetadataPath = odp.root(odataProductPath).node(ODATA_XML_PLACEHOLDER).value();
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
    protected String getProductUrl(EOData product) {
        throw new RuntimeException("This should not be called in this class");
    }

    @Override
    protected String getMetadataUrl(EOData product) {
        final Sentinel2ProductHelper helper = Sentinel2ProductHelper.createHelper(product.getName());
        String metadataFile = helper.getMetadataFileName();
        return odataMetadataPath.replace(ODATA_UUID, product.getId())
                .replace(ODATA_PRODUCT_NAME, product.getName())
                .replace(ODATA_XML_PLACEHOLDER, metadataFile);
    }

    @Override
    protected Path download(EOData product) throws IOException {
        Sentinel2ProductHelper helper = Sentinel2ProductHelper.createHelper(product.getName());
        String tileId = helper.getTileIdentifier();
        if (tileId != null && this.filteredTiles != null && !this.filteredTiles.contains(tileId)) {
            logger.warning(String.format("The product %s did not contain any tiles from the tile list", product.getName()));
            return null;
        } else {
            return downloadImpl(product);
        }
    }

    private Path downloadImpl(EOData product) throws IOException {
        Path rootPath = null;
        String url;
        Utilities.ensureExists(Paths.get(destination));
        String productName = product.getName();
        boolean isL1C = "Level-1C".equals(product.getAttributeValue("processinglevel"));
        Sentinel2ProductHelper helper = Sentinel2ProductHelper.createHelper(productName);
        if ("13".equals(helper.getVersion())) {
            currentStep = "Archive";
            url = odataArchivePath.replace(ODATA_UUID, product.getId());
            rootPath = Paths.get(destination, productName + ".zip");
            rootPath = downloadFile(url, rootPath, NetUtils.getAuthToken());
        }
        if (rootPath == null || !Files.exists(rootPath)) {
            rootPath = Utilities.ensureExists(Paths.get(destination, productName + ".SAFE"));
            url = getMetadataUrl(product);
            Path metadataFile = rootPath.resolve(helper.getMetadataFileName());
            currentStep = "Metadata";
            downloadFile(url, metadataFile, NetUtils.getAuthToken());
            if (Files.exists(metadataFile)) {
                List<String> allLines = Files.readAllLines(metadataFile);
                List<String> metaTileNames = Utilities.filter(allLines, "<Granule" + ("13".equals(helper.getVersion()) ? "s" : " "));
                boolean hasTiles = updateMedatata(metadataFile, allLines);
                if (hasTiles) {
                    Path tilesFolder = Utilities.ensureExists(rootPath.resolve(FOLDER_GRANULE));
                    Utilities.ensureExists(rootPath.resolve(FOLDER_AUXDATA));
                    Path dataStripFolder = Utilities.ensureExists(rootPath.resolve(FOLDER_DATASTRIP));
                    Map<String, String> tileNames = new HashMap<>();
                    String dataStripId = null;
                    String skippedTiles = "";
                    for (String tileName : metaTileNames) {
                        String tileId;
                        if (isL1C) {
                            tileId = tileName.substring(0, tileName.lastIndexOf(NAME_SEPARATOR));
                            tileId = tileId.substring(tileId.lastIndexOf(NAME_SEPARATOR) + 2);
                        } else {
                            int idx = tileName.lastIndexOf(NAME_SEPARATOR + "T");
                            tileId = tileName.substring(idx + 2, idx + 7);
                        }
                        if (filteredTiles == null || filteredTiles.size() == 0 || filteredTiles.contains(tileId)) {
                            String granuleId = Utilities.getAttributeValue(tileName, XML_ATTR_GRANULE_ID);
                            if (dataStripId == null) {
                                dataStripId = Utilities.getAttributeValue(tileName, XML_ATTR_DATASTRIP_ID);
                            }
                            String granule = helper.getGranuleFolder(dataStripId, granuleId);
                            tileNames.put(granuleId, odataTilePath.replace(ODATA_UUID, product.getId())
                                    .replace(ODATA_PRODUCT_NAME, productName)
                                    .replace("${tile}", granule));
                        } else {
                            skippedTiles += tileId + " ";
                        }
                    }
                    if (skippedTiles.trim().length() > 0) {
                        logger.fine(String.format("Skipped tiles: %s", skippedTiles));
                    }
                    String count = String.valueOf(tileNames.size());
                    int tileCounter = 1;
                    ODataPath pathBuilder = new ODataPath();
                    for (Map.Entry<String, String> entry : tileNames.entrySet()) {
                        long start = System.currentTimeMillis();
                        currentStep = "Tile " + String.valueOf(tileCounter++) + "/" + count;
                        String tileUrl = entry.getValue();
                        String granuleId = entry.getKey();
                        String tileName = helper.getGranuleFolder(dataStripId, granuleId);
                        Path tileFolder = Utilities.ensureExists(tilesFolder.resolve(tileName));
                        Path auxData = Utilities.ensureExists(tileFolder.resolve(FOLDER_AUXDATA));
                        Path imgData = Utilities.ensureExists(tileFolder.resolve(FOLDER_IMG_DATA));
                        Path qiData = Utilities.ensureExists(tileFolder.resolve(FOLDER_QI_DATA));
                        String metadataName = helper.getGranuleMetadataFileName(granuleId);
                        Path tileMetaFile = downloadFile(pathBuilder.root(tileUrl).node(metadataName).value(), tileFolder.resolve(metadataName), NetUtils.getAuthToken());
                        if (tileMetaFile != null) {
                            if (Files.exists(tileMetaFile)) {
                                if (isL1C) {
                                    for (String bandFileName : l1cBandFiles) {
                                        downloadFile(pathBuilder.root(tileUrl)
                                                             .node(FOLDER_IMG_DATA)
                                                             .node(helper.getBandFileName(granuleId, bandFileName))
                                                             .value(),
                                                     imgData.resolve(helper.getBandFileName(granuleId, bandFileName)),
                                                     NetUtils.getAuthToken());
                                    }
                                } else {
                                    for (Map.Entry<String, Set<String>> resEntry : l2aBandFiles.entrySet()) {
                                        Path imgDataRes = Utilities.ensureExists(imgData.resolve(resEntry.getKey()));
                                        for (String bandFileName : resEntry.getValue()) {
                                            downloadFile(pathBuilder.root(tileUrl)
                                                                 .node(FOLDER_IMG_DATA)
                                                                 .node(resEntry.getKey())
                                                                 .node(helper.getBandFileName(granuleId, bandFileName))
                                                                 .value(),
                                                         imgDataRes.resolve(helper.getBandFileName(granuleId, bandFileName)),
                                                         NetUtils.getAuthToken());
                                        }
                                    }
                                }
                                List<String> lines = Utilities.filter(Files.readAllLines(metadataFile),
                                                                      "<MASK_FILENAME");
                                for (String line : lines) {
                                    line = line.trim();
                                    int firstTagCloseIdx = line.indexOf(">") + 1;
                                    int secondTagBeginIdx = line.indexOf("<", firstTagCloseIdx);
                                    String maskFileName = line.substring(firstTagCloseIdx, secondTagBeginIdx);
                                    maskFileName = maskFileName.substring(maskFileName.lastIndexOf(URL_SEPARATOR) + 1);
                                    downloadFile(pathBuilder.root(tileUrl)
                                                         .node(FOLDER_QI_DATA)
                                                         .node(maskFileName)
                                                         .value(),
                                                 qiData.resolve(maskFileName),
                                                 NetUtils.getAuthToken());
                                }
                                if (!isL1C) {
                                    for (String maskFileName : l2aMasks) {
                                        downloadFile(pathBuilder.root(tileUrl)
                                                             .node(FOLDER_QI_DATA)
                                                             .node(helper.getBandFileName(granuleId, maskFileName))
                                                             .value(),
                                                     qiData.resolve(helper.getBandFileName(granuleId, maskFileName)),
                                                     NetUtils.getAuthToken());
                                    }
                                }
                                logger.info(String.format("Tile download completed in %s", Utilities.formatTime(System.currentTimeMillis() - start)));
                            } else {
                                logger.warning(String.format("File %s was not downloaded", tileMetaFile.getFileName()));
                            }
                        }
                    }
                    if (dataStripId != null) {
                        String dsFolder = helper.getDatastripFolder(dataStripId);
                        String dsFileName = helper.getDatastripMetadataFileName(dataStripId);
                        String dataStripPath = pathBuilder.root(odataProductPath.replace(ODATA_UUID, product.getId())
                                                                        .replace(ODATA_PRODUCT_NAME, productName))
                                .node(FOLDER_DATASTRIP).node(dsFolder)
                                .node(dsFileName)
                                .value();
                        Path dataStrip = Utilities.ensureExists(dataStripFolder.resolve(dsFolder));
                        downloadFile(dataStripPath, dataStrip.resolve(dsFileName), NetUtils.getAuthToken());
                    }
                } else {
                    Files.deleteIfExists(metadataFile);
                    rootPath = null;
                    logger.warning(String.format("The product %s did not contain any tiles from the tile list", productName));
                }
            } else {
                logger.warning(String.format("The product %s was not found", productName));
                rootPath = null;
            }
        }
        return rootPath;
    }

    private boolean updateMedatata(Path metaFile, List<String> originalLines) throws IOException {
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
}
