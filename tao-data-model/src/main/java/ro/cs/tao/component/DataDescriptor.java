package ro.cs.tao.component;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.serialization.CRSAdapter;
import ro.cs.tao.serialization.GeometryAdapter;

import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.net.URI;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "dataDescriptor")
public class DataDescriptor {
    private DataFormat formatType;
    private Geometry geometry;
    private CoordinateReferenceSystem crs;
    private SensorType sensorType;
    private Dimension dimension;
    private String location;

    public DataFormat getFormatType() { return formatType; }
    public void setFormatType(DataFormat formatType) { this.formatType = formatType; }

    public String getGeometry() {
        try {
            return new GeometryAdapter().unmarshal(geometry);
        } catch (Exception e) {
            return null;
        }
    }
    public void setGeometry(String geometryAsText) {
        try {
            this.geometry = new GeometryAdapter().marshal(geometryAsText);
        } catch (Exception ignored) { }
    }

    public String getCrs() {
        try {
            return new CRSAdapter().unmarshal(this.crs);
        } catch (Exception e) {
            return null;
        }
    }

    public void setCrs(String crsCode) {
        try {
            this.crs = new CRSAdapter().marshal(crsCode);
        } catch (Exception ignored) { }
    }

    public SensorType getSensorType() { return sensorType; }
    public void setSensorType(SensorType sensorType) { this.sensorType = sensorType; }

    public Dimension getDimension() { return dimension; }
    public void setDimension(Dimension dimension) { this.dimension = dimension; }

    public String getLocation() { return this.location; }
    public void setLocation(String value) {
        //noinspection ResultOfMethodCallIgnored
        URI.create(value);
        this.location = value;
    }
}
