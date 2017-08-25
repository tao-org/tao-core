package ro.cs.tao.eodata.serialization;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author kraftek
 * @date 2/28/2017
 */
public class GeometryAdapter extends XmlAdapter<Geometry, String> {

    @Override
    public String unmarshal(Geometry v) throws Exception {
        return v.toText();
    }

    @Override
    public Geometry marshal(String v) throws Exception {
        Geometry result = null;
        if (v != null && !v.isEmpty()) {
            WKTReader reader = new WKTReader();
            result = reader.read(v);
        }
        return result;
    }
}
