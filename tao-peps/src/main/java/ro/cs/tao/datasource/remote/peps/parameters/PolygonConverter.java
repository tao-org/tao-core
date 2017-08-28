package ro.cs.tao.datasource.remote.peps.parameters;

import ro.cs.tao.datasource.converters.ConversionException;
import ro.cs.tao.datasource.converters.DefaultConverter;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.eodata.Polygon2D;

import java.awt.geom.Rectangle2D;

/**
 * @author Cosmin Cara
 */
public class PolygonConverter extends DefaultConverter {

    public PolygonConverter(QueryParameter parameter) {
        super(parameter);
        if (!Polygon2D.class.equals(parameter.getType())) {
            throw new IllegalArgumentException("Incorrect parameter type");
        }
    }

    @Override
    public String stringValue() throws ConversionException {
        Polygon2D polygon2D = (Polygon2D) this.parameter.getValue();
        if (polygon2D != null && polygon2D.getNumPoints() > 0) {
            Rectangle2D bounds2D = polygon2D.getBounds2D();
            return String.valueOf(bounds2D.getMinX()) + "," + bounds2D.getMinY() + "," +
                    bounds2D.getMaxX() + "," + bounds2D.getMinY();
        } else {
            return null;
        }
    }
}