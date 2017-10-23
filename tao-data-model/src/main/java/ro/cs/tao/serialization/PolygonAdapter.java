package ro.cs.tao.serialization;

import ro.cs.tao.eodata.Polygon2D;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Cosmin Cara
 */
public class PolygonAdapter extends XmlAdapter<String, Polygon2D> {
    @Override
    public Polygon2D unmarshal(String v) throws Exception {
        return Polygon2D.fromWKT(v);
    }

    @Override
    public String marshal(Polygon2D v) throws Exception {
        return v.toWKT();
    }
}
