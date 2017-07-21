package ro.cs.tao.datasource.remote.aws;

import ro.cs.tao.datasource.common.DataQuery;
import ro.cs.tao.datasource.common.DataSource;
import ro.cs.tao.datasource.common.ParameterProvider;
import ro.cs.tao.datasource.common.QueryException;
import ro.cs.tao.datasource.common.QueryParameter;
import ro.cs.tao.datasource.common.json.Result;
import ro.cs.tao.datasource.common.json.ResultParser;
import ro.cs.tao.datasource.remote.AbstractDownloader;
import ro.cs.tao.datasource.remote.aws.helpers.LandsatProductHelper;
import ro.cs.tao.datasource.util.Logger;
import ro.cs.tao.datasource.util.NetUtils;
import ro.cs.tao.datasource.util.Polygon2D;
import ro.cs.tao.eodata.EOData;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.eodata.serialization.GeometryAdapter;
import ro.cs.tao.eodata.util.Conversions;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
class Landsat8Query extends DataQuery<EOData> {
    private static final String L8_SEARCH_URL_SUFFIX = "?delimiter=/&prefix=";
    private static final DateTimeFormatter fileDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public Landsat8Query(DataSource source, ParameterProvider parameterProvider) {
        super(source, parameterProvider);
    }

    @Override
    protected List<EOData> executeImpl() throws QueryException {
        String sensingStart, sensingEnd;
        LandsatCollection productType;
        double cloudFilter = 100.;
        Map<String, EOProduct> results = new LinkedHashMap<>();

        LocalDate todayDate = LocalDate.now();

        //http://sentinel-s2-l1c.s3.amazonaws.com/?delimiter=/&prefix=c1/L8/
        Calendar startDate = Calendar.getInstance();
        QueryParameter currentParameter = this.parameters.get("sensingStart");
        if (currentParameter != null) {
            sensingStart = currentParameter.getValueAsFormattedDate(dateFormat.toPattern());
        } else {
            sensingStart = todayDate.minusDays(30).format(fileDateFormat);
        }
        currentParameter = this.parameters.get("sensingEnd");
        if (currentParameter != null) {
            sensingEnd = currentParameter.getValueAsFormattedDate(dateFormat.toPattern());
        } else {
            sensingEnd = todayDate.format(fileDateFormat);
        }
        try {
            startDate.setTime(dateFormat.parse(sensingStart));
            Calendar endDate = Calendar.getInstance();
            endDate.setTime(dateFormat.parse(sensingEnd));
            currentParameter = this.parameters.get("productType");
            if (currentParameter != null) {
                productType = Enum.valueOf(LandsatCollection.class, currentParameter.getValueAsString());
            } else {
                productType = LandsatCollection.T1;
            }

            currentParameter = this.parameters.get("cloudcoverpercentage");
            if (currentParameter != null) {
                cloudFilter = currentParameter.getValueAsDouble();
            }


            final String baseUrl = this.source.getConnectionString();

            String path = this.parameters.get("path").getValueAsString();
            String row = this.parameters.get("row").getValueAsString();
            String tileUrl = baseUrl + path + AbstractDownloader.URL_SEPARATOR + row + AbstractDownloader.URL_SEPARATOR;
            Result productResult = ResultParser.parse(NetUtils.getResponseAsString(tileUrl));
            if (productResult.getCommonPrefixes() != null) {
                Set<String> names = productResult.getCommonPrefixes().stream()
                        .map(p -> p.replace(productResult.getPrefix(), "").replace(productResult.getDelimiter(), ""))
                        .collect(Collectors.toSet());
                for (String name : names) {
                    try {
                        if (name.endsWith(productType.toString())) {
                            LandsatProductHelper temporaryDescriptor = new LandsatProductHelper(name);
                            Calendar productDate = temporaryDescriptor.getAcquisitionDate();
                            if (startDate.before(productDate) && endDate.after(productDate)) {
                                String jsonTile = tileUrl + name + AbstractDownloader.URL_SEPARATOR + name + "_MTL.json";
                                jsonTile = jsonTile.replace(L8_SEARCH_URL_SUFFIX, "");
                                double clouds = getTileCloudPercentage(jsonTile);
                                if (clouds > cloudFilter) {
                                    productDate.add(Calendar.MONTH, -1);
                                    Logger.getRootLogger().warn(
                                            String.format("Tile %s from %s has %.2f %% clouds",
                                                          path + row, dateFormat.format(productDate.getTime()), clouds));
                                } else {
                                    EOProduct product = parseProductJson(jsonTile);
                                    results.put(product.getName(), product);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getRootLogger().warn("Could not parse product %s: %s", name, ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Logger.getRootLogger().warn(e.getMessage());
        }
        Logger.getRootLogger().info("Query returned %s products", results.size());
        return new ArrayList<>(results.values());
    }

    private EOProduct parseProductJson(String jsonUrl) throws Exception {
        JsonReader reader = null;
        EOProduct product = null;
        try (InputStream inputStream = new URI(jsonUrl).toURL().openStream()) {
            reader = Json.createReader(inputStream);
            JsonObject obj = reader.readObject()
                    .getJsonObject("L1_METADATA_FILE")
                    .getJsonObject("METADATA_FILE_INFO");
            product = new EOProduct();
            product.setId(obj.getString("LANDSAT_SCENE_ID"));
            if (obj.containsKey("LANDSAT_PRODUCT_ID")) {
                product.setName(obj.getString("LANDSAT_PRODUCT_ID"));
            } else {
                product.setName(obj.getString("LANDSAT_SCENE_ID"));
            }
            product.setType(DataFormat.RASTER);
            product.setSensorType(SensorType.OPTICAL);
            obj = reader.readObject().getJsonObject("L1_METADATA_FILE").getJsonObject("PRODUCT_METADATA");
            product.setAcquisitionDate(dateFormat.parse(obj.getString("DATE_ACQUIRED")));
            product.setWidth(obj.getInt("REFLECTIVE_SAMPLES"));
            product.setHeight(obj.getInt("REFLECTIVE_LINES"));
            Polygon2D footprint = new Polygon2D();
            footprint.append(obj.getJsonNumber("CORNER_UL_LAT_PRODUCT").doubleValue(),
                             obj.getJsonNumber("CORNER_UL_LON_PRODUCT").doubleValue());
            footprint.append(obj.getJsonNumber("CORNER_UR_LAT_PRODUCT").doubleValue(),
                             obj.getJsonNumber("CORNER_UR_LON_PRODUCT").doubleValue());
            footprint.append(obj.getJsonNumber("CORNER_LR_LAT_PRODUCT").doubleValue(),
                             obj.getJsonNumber("CORNER_LR_LON_PRODUCT").doubleValue());
            footprint.append(obj.getJsonNumber("CORNER_LL_LAT_PRODUCT").doubleValue(),
                             obj.getJsonNumber("CORNER_LL_LON_PRODUCT").doubleValue());
            footprint.append(obj.getJsonNumber("CORNER_UL_LAT_PRODUCT").doubleValue(),
                             obj.getJsonNumber("CORNER_UL_LON_PRODUCT").doubleValue());
            product.setGeometry(new GeometryAdapter().marshal(footprint.toWKT()));

            obj = reader.readObject().getJsonObject("L1_METADATA_FILE").getJsonObject("MIN_MAX_PIXEL_VALUE");
            product.setPixelType(Conversions.pixelTypeFromRange(obj.getInt("QUANTIZE_CAL_MIN_BAND_1"),
                                                                obj.getInt("QUANTIZE_CAL_MAX_BAND_1")));
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return product;
    }

    private double getTileCloudPercentage(String jsonUrl) throws IOException, URISyntaxException {
        JsonReader reader = null;
        try (InputStream inputStream = new URI(jsonUrl).toURL().openStream()) {
            reader = Json.createReader(inputStream);
            JsonObject obj = reader.readObject();
            return obj.getJsonObject("L1_METADATA_FILE")
                    .getJsonObject("IMAGE_ATTRIBUTES")
                    .getJsonNumber("CLOUD_COVER").doubleValue();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
