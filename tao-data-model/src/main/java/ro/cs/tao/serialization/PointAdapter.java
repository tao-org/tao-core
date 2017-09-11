package ro.cs.tao.serialization;

import com.vividsolutions.jts.geom.Coordinate;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author kraftek
 * @date 2/28/2017
 */
public class PointAdapter extends XmlAdapter<Coordinate, String> {

    @Override
    public String unmarshal(Coordinate v) throws Exception {
        if (v != null) {
            String vs = v.toString();
            vs = vs.substring(1, vs.length() - 1);
            return vs;
        } else
            return null;
    }

    @Override
    public Coordinate marshal(String v) throws Exception {
        Coordinate result = null;
        if (v != null && !v.isEmpty()) {
            String[] tokens = v.split(",");
            switch (tokens.length) {
                case 2:
                    result = new Coordinate(Double.parseDouble(tokens[0]),
                                            Double.parseDouble(tokens[1]),
                                            0);
                    break;
                case 3:
                    result = new Coordinate(Double.parseDouble(tokens[0]),
                                            Double.parseDouble(tokens[1]),
                                            Double.parseDouble(tokens[2]));
                    break;
                default:
                    throw new SerializationException("Invalid string coordinate");
            }
        }
        return result;
    }
}
