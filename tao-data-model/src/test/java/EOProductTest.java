import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.PixelType;
import ro.cs.tao.eodata.enums.ProductStatus;
import ro.cs.tao.eodata.enums.SensorType;

import java.time.LocalDateTime;

/**
 * @author Cosmin Cara
 */
public class EOProductTest extends BaseSerializationTest<EOProduct> {
    @Override
    protected String referenceJSON() {
        return "{\"id\":\"1234567890\",\"name\":\"Product\",\"formatType\":0,\"geometry\":\"POLYGON ((0 0, 1 1, 2 2, 3 3, 0 0))\",\"attributes\":[{\"name\":\"attribute1\",\"value\":\"value1\"},{\"name\":\"attribute2\",\"value\":\"value2\"}],\"crs\":\"EPSG:4326\",\"location\":\"http://some.url.com/some_location/product.zip\",\"entryPoint\":null,\"visibility\":null,\"productStatus\":2,\"sensorType\":\"OPTICAL\",\"acquisitionDate\":\"2017-09-01T18:33:47\",\"pixelType\":2,\"productType\":\"Satellite5\",\"width\":1024,\"height\":1024,\"approximateSize\":1234,\"processingDate\":null,\"quicklookLocation\":null,\"refs\":null,\"satelliteName\":\"Satellite-5\"}";
    }

    @Override
    protected String referenceXML() {
        return "<eoData><id>1234567890</id><name>Product</name><formatType>0</formatType><geometry>POLYGON ((0 0, 1 1, 2 2, 3 3, 0 0))</geometry><attributes><attributes><name>attribute1</name><value>value1</value></attributes><attributes><name>attribute2</name><value>value2</value></attributes></attributes><crs>EPSG:4326</crs><location>http://some.url.com/some_location/product.zip</location><entryPoint/><visibility/><productStatus>2</productStatus><sensorType>OPTICAL</sensorType><acquisitionDate>2017-09-01T18:33:47</acquisitionDate><pixelType>2</pixelType><productType>Satellite5</productType><width>1024</width><height>1024</height><approximateSize>1234</approximateSize><processingDate/><quicklookLocation/><satelliteName>Satellite-5</satelliteName></eoData>";
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        entity = new EOProduct() {{
            setName("Product");
            setAcquisitionDate(LocalDateTime.of(2017,9,1,18,33,47));
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
            setApproximateSize(1234);
            setSatelliteName("Satellite-5");
            setProductStatus(ProductStatus.DOWNLOADED);
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
