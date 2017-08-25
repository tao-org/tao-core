package ro.cs.tao.datasource.remote.aws;

import org.geotools.referencing.CRS;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSource;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.param.ParameterProvider;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.AbstractDownloader;
import ro.cs.tao.datasource.remote.aws.internal.AwsResult;
import ro.cs.tao.datasource.remote.aws.internal.IntermediateParser;
import ro.cs.tao.datasource.util.Logger;
import ro.cs.tao.datasource.util.NetUtils;
import ro.cs.tao.eodata.EOData;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.PixelType;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.eodata.serialization.GeometryAdapter;

import javax.json.Json;
import javax.json.JsonArray;
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
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
class Sentinel2Query extends DataQuery<EOData> {
    private static final String S2_SEARCH_URL_SUFFIX = "?delimiter=/&prefix=";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateTimeFormatter fileDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    Sentinel2Query(DataSource source, ParameterProvider parameterProvider) {
        super(source, parameterProvider);
    }

    @Override
    protected List<EOData> executeImpl() throws QueryException {
        QueryParameter currentParameter = this.parameters.get("platformName");
        if (currentParameter == null ||
                !"S2".equals(currentParameter.getValueAsString())) {
            throw new QueryException("Wrong [platformName] parameter");
        }
        Map<String, EOProduct> results = new LinkedHashMap<>();
        try {
            String sensingStart, sensingEnd;
            double cloudFilter = 100.;
            int relativeOrbit = 0;

            Set<String> tiles = new HashSet<>();
            currentParameter = this.parameters.get("tileId");
            if (currentParameter != null) {
                tiles.add(currentParameter.getValueAsString());
            } else if ((currentParameter = this.parameters.get("footprint")) != null) {
                Polygon2D aoi = Polygon2D.fromWKT(currentParameter.getValueAsString());
                tiles.addAll(Sentinel2TileExtent.getInstance().intersectingTiles(aoi.getBounds2D()));
            } else {
                throw new QueryException("Either [tileId] or [footprint] have to be given.");
            }

            Calendar startDate = Calendar.getInstance();
            Calendar endDate = Calendar.getInstance();
            LocalDate todayDate = LocalDate.now();

            currentParameter = this.parameters.get("beginPosition");
            if (currentParameter != null) {
                sensingStart = currentParameter.getValueAsFormattedDate(dateFormat.toPattern());
            } else {
                sensingStart = todayDate.minusDays(30).format(fileDateFormat);
            }
            startDate.setTime(dateFormat.parse(sensingStart));
            currentParameter = this.parameters.get("endPosition");
            if (currentParameter != null) {
                sensingEnd = dateFormat.format(new Date(System.currentTimeMillis()));
            } else {
                sensingEnd = todayDate.format(fileDateFormat);
            }
            endDate.setTime(dateFormat.parse(sensingEnd));
            //http://sentinel-s2-l1c.s3.amazonaws.com/?delimiter=/&prefix=tiles/15/R/TM/

            currentParameter = this.parameters.get("cloudcoverpercentage");
            if (currentParameter != null) {
                cloudFilter = currentParameter.getValueAsDouble();
            }

            currentParameter = this.parameters.get("relativeOrbitNumber");
            if (currentParameter != null) {
                relativeOrbit = currentParameter.getValueAsInt();
            }

            int yearStart = startDate.get(Calendar.YEAR);
            int monthStart = startDate.get(Calendar.MONTH) + 1;
            int dayStart = startDate.get(Calendar.DAY_OF_MONTH);
            int yearEnd = endDate.get(Calendar.YEAR);
            int monthEnd = endDate.get(Calendar.MONTH) + 1;
            int dayEnd = endDate.get(Calendar.DAY_OF_MONTH);
            for (String tile : tiles) {
                String utmCode = tile.substring(0, 2);
                String latBand = tile.substring(2, 3);
                String square = tile.substring(3, 5);
                String tileUrl = this.source.getConnectionString() + utmCode +
                        AbstractDownloader.URL_SEPARATOR + latBand + AbstractDownloader.URL_SEPARATOR +
                        square + AbstractDownloader.URL_SEPARATOR;
                for (int year = yearStart; year <= yearEnd; year++) {
                    String yearUrl = tileUrl + String.valueOf(year) + AbstractDownloader.URL_SEPARATOR;
                    AwsResult yearResult = IntermediateParser.parse(NetUtils.getResponseAsString(yearUrl));
                    if (yearResult.getCommonPrefixes() != null) {
                        Set<Integer> months = yearResult.getCommonPrefixes().stream()
                                .map(p -> {
                                    String tmp = p.replace(yearResult.getPrefix(), "");
                                    return Integer.parseInt(tmp.substring(0, tmp.indexOf(yearResult.getDelimiter())));
                                }).collect(Collectors.toSet());
                        int monthS = year == yearStart ? monthStart : 1;
                        int monthE = year == yearEnd ? monthEnd : 12;
                        for (int month = monthS; month <= monthE; month++) {
                            if (months.contains(month)) {
                                String monthUrl = yearUrl + String.valueOf(month) + AbstractDownloader.URL_SEPARATOR;
                                AwsResult monthResult = IntermediateParser.parse(NetUtils.getResponseAsString(monthUrl));
                                if (monthResult.getCommonPrefixes() != null) {
                                    Set<Integer> days = monthResult.getCommonPrefixes().stream()
                                            .map(p -> {
                                                String tmp = p.replace(monthResult.getPrefix(), "");
                                                return Integer.parseInt(tmp.substring(0, tmp.indexOf(monthResult.getDelimiter())));
                                            }).collect(Collectors.toSet());
                                    int dayS = month == monthS ? dayStart : 1;
                                    Calendar calendar = new Calendar.Builder().setDate(year, month + 1, 1).build();
                                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                                    int dayE = month == monthE ? dayEnd : calendar.get(Calendar.DAY_OF_MONTH);
                                    for (int day = dayS; day <= dayE; day++) {
                                        if (days.contains(day)) {
                                            String dayUrl = monthUrl + String.valueOf(day) + AbstractDownloader.URL_SEPARATOR;
                                            AwsResult dayResult = IntermediateParser.parse(NetUtils.getResponseAsString(dayUrl));
                                            if (dayResult.getCommonPrefixes() != null) {
                                                Set<Integer> sequences = dayResult.getCommonPrefixes().stream()
                                                        .map(p -> {
                                                            String tmp = p.replace(dayResult.getPrefix(), "");
                                                            return Integer.parseInt(tmp.substring(0, tmp.indexOf(dayResult.getDelimiter())));
                                                        }).collect(Collectors.toSet());
                                                for (int sequence : sequences) {
                                                    String jsonTile = dayUrl + String.valueOf(sequence) +
                                                            AbstractDownloader.URL_SEPARATOR + "tileInfo.json";
                                                    jsonTile = jsonTile.replace(S2_SEARCH_URL_SUFFIX, "");
                                                    EOProduct product = new EOProduct();
                                                    double clouds = getTileCloudPercentage(jsonTile, product);
                                                    if (clouds > cloudFilter) {
                                                        Calendar instance = new Calendar.Builder().setDate(year, month - 1, day).build();
                                                        Logger.getRootLogger().warn(
                                                                String.format("Tile %s from %s has %.2f %% clouds",
                                                                              tile, dateFormat.format(instance.getTime()), clouds));
                                                    } else {
                                                        String jsonProduct = dayUrl + String.valueOf(sequence) +
                                                                AbstractDownloader.URL_SEPARATOR + "productInfo.json";
                                                        jsonProduct = jsonProduct.replace("?delimiter=/&prefix=", "");
                                                        parseProductJson(jsonProduct, product);
                                                        if (relativeOrbit == 0 ||
                                                                product.getName().contains("_R" + String.format("%03d", relativeOrbit))) {
                                                            results.put(product.getName(), product);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getRootLogger().warn(ex.getMessage());
        }
        Logger.getRootLogger().info("Query returned %s products", results.size());
        return new ArrayList<>(results.values());
    }

    private void parseProductJson(String jsonUrl, EOProduct product) throws Exception {
        JsonReader reader = null;
        try (InputStream inputStream = new URI(jsonUrl).toURL().openStream()) {
            reader = Json.createReader(inputStream);
            JsonObject obj = reader.readObject();
            product.setType(DataFormat.RASTER);
            product.setSensorType(SensorType.OPTICAL);
            product.setPixelType(PixelType.UINT16);
            product.setName(obj.getString("name"));
            product.setId(obj.getString("id"));
            product.setLocation(this.source.getConnectionString()
                                           .replace(S2_SEARCH_URL_SUFFIX, "") + obj.getString("path"));
            product.setAcquisitionDate(dateFormat.parse(obj.getString("timestamp")));
            product.setWidth(-1);
            product.setHeight(-1);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private double getTileCloudPercentage(String jsonUrl, EOProduct product) throws IOException, URISyntaxException {
        JsonReader reader = null;
        if (product == null) {
            product = new EOProduct();
        }
        try (InputStream inputStream = new URI(jsonUrl).toURL().openStream()) {
            reader = Json.createReader(inputStream);
            JsonObject obj = reader.readObject();
            double clouds = obj.getJsonNumber("cloudyPixelPercentage").doubleValue();
            product.addAttribute("cloudcoverpercentage", String.valueOf(clouds));
            try {
                product.setCrs(CRS.decode(obj.getJsonObject("tileGeometry")
                                .getJsonObject("crs")
                                .getJsonObject("properties")
                                .getString("name")));
                JsonArray coords = obj.getJsonObject("tileGeometry").getJsonArray("coordinates").getJsonArray(0);
                Polygon2D polygon2D = new Polygon2D();
                polygon2D.append(coords.getJsonArray(0).getInt(1),
                                 coords.getJsonArray(0).getInt(0));
                polygon2D.append(coords.getJsonArray(1).getInt(1),
                                 coords.getJsonArray(1).getInt(0));
                polygon2D.append(coords.getJsonArray(2).getInt(1),
                                 coords.getJsonArray(2).getInt(0));
                polygon2D.append(coords.getJsonArray(3).getInt(1),
                                 coords.getJsonArray(3).getInt(0));
                polygon2D.append(coords.getJsonArray(4).getInt(1),
                                 coords.getJsonArray(4).getInt(0));
                product.setGeometry(new GeometryAdapter().marshal(polygon2D.toWKT()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return clouds;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
