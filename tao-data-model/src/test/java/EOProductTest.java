import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.PixelType;
import ro.cs.tao.eodata.enums.SensorType;

import java.text.SimpleDateFormat;

/**
 * @author Cosmin Cara
 */
public class EOProductTest extends BaseSerializationTest<EOProduct> {
    @Override
    protected String referenceJSON() {
        return "{\n" +
                "      \"crs\" : \"EPSG:4326\",\n" +
                "      \"geometry\" : \"POLYGON ((0 0, 1 1, 2 2, 3 3, 0 0))\",\n" +
                "      \"id\" : \"1234567890\",\n" +
                "      \"formatType\" : \"1\",\n" +
                "      \"acquisitionDate\" : \"2017-09-01T18:33:47+03:00\",\n" +
                "      \"height\" : 1024,\n" +
                "      \"pixelType\" : \"2\",\n" +
                "      \"productType\" : \"Satellite-5\",\n" +
                "      \"sensorType\" : \"0\",\n" +
                "      \"width\" : 1024\n" +
                "}";
    }

    @Override
    protected String referenceXML() {
        return "<eoData>\n" +
                "    <crs>EPSG:4326</crs>\n" +
                "    <geometry>POLYGON ((0 0, 1 1, 2 2, 3 3, 0 0))</geometry>\n" +
                "    <id>1234567890</id>\n" +
                "    <formatType>1</formatType>\n" +
                "    <acquisitionDate>2017-09-01T18:33:47+03:00</acquisitionDate>\n" +
                "    <height>1024</height>\n" +
                "    <pixelType>2</pixelType>\n" +
                "    <productType>Satellite-5</productType>\n" +
                "    <sensorType>0</sensorType>\n" +
                "    <width>1024</width>\n" +
                "    <attributes>\n" +
                "        <attribute>\n" +
                "            <name>attributeX</name>\n" +
                "            <value>valueX</value>\n" +
                "        </attribute>\n" +
                "    </attributes>\n" +
                "</eoData>";
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        entity = new EOProduct() {{
            setName("Product");
            setAcquisitionDate(new SimpleDateFormat("yyyyMMdd'T'hh:mm:ss").parse("20170901T18:33:47"));
            setLocation("http://some.url.com/some_location/product.zip");
            setFormatType(DataFormat.RASTER);
            setProductType("Satellite-5");
            setPixelType(PixelType.UINT16);
            setCrs("EPSG:4326");
            setGeometry("POLYGON((0 0, 1 1, 2 2, 3 3, 0 0))");
            setHeight(1024);
            setWidth(1024);
            setId("1234567890");
            setSensorType(SensorType.OPTICAL);
            addAttribute("attribute1", "value1");
            addAttribute("attribute2", "value2");
        }};
    }

    @Test
    public void deserializeFromJSON() throws Exception {
        EOProduct object = deserializeJson();
        Assert.assertEquals("EPSG:4326", object.getCrs());
    }

    @Test
    public void deserializeFromXML() throws Exception {
        EOProduct object = deserializeXml();
        Assert.assertEquals("EPSG:4326", object.getCrs());
    }
}
