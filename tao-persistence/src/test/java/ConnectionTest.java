import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import ro.cs.tao.serialization.GeometryAdapter;
import ro.cs.tao.utils.Assert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
public class ConnectionTest {

    @Test
    public void testConnection() throws ClassNotFoundException, SQLException{ // trebuie h2 setat in pom.xml + Dbconfiguration; run -> /tao-core/db
        Class.forName("org.h2.Driver");
        Connection connection = DriverManager.getConnection("jdbc:h2:./../db/taodata;MODE=PostgreSQL;INIT=RUNSCRIPT FROM 'classpath:schema.sql'\\;RUNSCRIPT FROM 'classpath:data.sql'", "tao", "tao");
//        Connection connection = DriverManager.getConnection("jdbc:h2:./../db/taodata", "tao", "tao");
        Assert.notNull(connection);
    }

    @Test
    public void testJson(){

        String time = "2025-05-09 18:32:26.466113";
        System.out.println(LocalDateTime.now());
//        Dimension d1 = new Dimension();
//        d1.setSize(100, 100);
//        Dimension d2 = null;
//
//        String s1 = JacksonUtil.toString(d1);
//        String s2 = JacksonUtil.toString(d2);
//
//        System.out.println(s1);
//        System.out.println(s2);

//       String dbData = "{\"allowed_values\":[{\"value\":\"docker\",\"display\":\"Docker\"},{\"value\":\"rancher\",\"display\":\"Rancher\"}]}";
//       Dimension d =  JacksonUtil.fromString(dbData, Dimension.class);
//        Assert.notNull(d);
    }

    @Test
    public void test_PolygonInsert() throws ParseException {
        String v = "SRID=4321;POLYGON ((30.942302 47.647812, 31.611786 47.647812, 31.611786 49.79279, 30.942302 49.79279, 30.942302 47.647812))";
        v = v.substring(10);
        Geometry result = null;

        WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(), 4326));
        result = reader.read(v);
//        ST_IsValid(ST_GeomFromText(v));

//        WKBReader reader = new WKBReader(new GeometryFactory(new PrecisionModel(), 4326));
//        result = reader.read(WKBReader.hexToBytes(v));
//        result.setSRID(4326);

//        WKTReader reader = new WKTReader(new GeometryFactory(new PrecisionModel(), 4326));
//        result = reader.read(v);


//        WKBReader doi = new WKBReader(new GeometryFactory(new PrecisionModel(), 4326));
//        result = doi.read(v);

//        WKBWriter wkbWriter = new WKBWriter();
//        byte[] wkb = wkbWriter.write(geometry);

//        Geometry s = new GeometryAdapter().unmarshal(result);
//        result.setSRID(4326);
        System.out.println(result);

    }
}
