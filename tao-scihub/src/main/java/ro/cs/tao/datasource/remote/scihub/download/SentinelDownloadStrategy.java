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

package ro.cs.tao.datasource.remote.scihub.download;

import ro.cs.tao.datasource.InterruptedException;
import ro.cs.tao.datasource.remote.DownloadStrategy;
import ro.cs.tao.datasource.remote.scihub.SciHubDataSource;
import ro.cs.tao.datasource.util.NetUtils;
import ro.cs.tao.datasource.util.Utilities;
import ro.cs.tao.eodata.EOProduct;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Cosmin Cara
 */
public class SentinelDownloadStrategy extends DownloadStrategy {

    static final String ODATA_XML_PLACEHOLDER = "${xmlname}";
    static final String ODATA_UUID = "${UUID}";
    static final String ODATA_PRODUCT_NAME = "${PRODUCT_NAME}";

    private static final Properties properties;

    String oDataBasePath;
    String odataArchivePath;

    static {
        properties = new Properties();
        try {
            properties.load(SciHubDataSource.class.getResourceAsStream("scihub.properties"));
        } catch (IOException ignored) {
        }
    }

    public SentinelDownloadStrategy(String targetFolder) {
        super(targetFolder, properties);
        ODataPath odp = new ODataPath();
        String scihubUrl = props.getProperty("scihub.product.url", "https://scihub.copernicus.eu/apihub/odata/v1");
        /*if (!NetUtils.isAvailable(scihubUrl)) {
            System.err.println(scihubUrl + " is not available!");
            scihubUrl = props.getProperty("scihub.product.backup.url", "https://scihub.copernicus.eu/dhus/odata/v1");
        }*/
        oDataBasePath = odp.root(scihubUrl + "/Products('${UUID}')").path();
        odataArchivePath = odp.root(scihubUrl + "/Products('${UUID}')").value();
    }

    @Override
    public String getProductUrl(EOProduct descriptor) {
        return odataArchivePath.replace(ODATA_UUID, descriptor.getId());
    }

    @Override
    protected String getMetadataUrl(EOProduct descriptor) {
        return null;
    }

    @Override
    public Path fetch(EOProduct product) throws IOException, InterruptedException {
        Utilities.ensureExists(Paths.get(destination));
        String productName = product.getName();
        currentStep = "Archive";
        Path rootPath = Paths.get(destination, productName + ".zip");
        return downloadFile(getProductUrl(product), rootPath, NetUtils.getAuthToken());
    }

    class ODataPath {
        private StringBuilder buffer;

        ODataPath() {
            buffer = new StringBuilder();
        }

        ODataPath root(String path) {
            buffer.setLength(0);
            buffer.append(path);
            return this;
        }

        ODataPath node(String nodeName) {
            buffer.append("/Nodes('").append(nodeName).append("')");
            return this;
        }

        String path() {
            return buffer.toString();
        }

        String value() {
            return buffer.toString() + "/$value";
        }
    }
}
