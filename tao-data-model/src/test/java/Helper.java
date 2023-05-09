import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.PixelType;
import ro.cs.tao.eodata.enums.ProductStatus;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.Serializer;
import ro.cs.tao.serialization.SerializerFactory;

import java.time.LocalDateTime;

public class Helper {

    public static void main(String[] args) throws Exception {
        EOProduct entity = new EOProduct() {{
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
        serialize(entity.getClass(), entity);
    }

    private static <T> void serialize(Class entityClass, T entity) throws SerializationException {
        final Serializer<T, String> jsonSerializer = SerializerFactory.create(entityClass, MediaType.JSON);
        final Serializer<T, String> xmlSerializer = SerializerFactory.create(entityClass, MediaType.XML);
        System.out.println(jsonSerializer.serialize(entity));
        System.out.println(xmlSerializer.serialize(entity));
    }
}
