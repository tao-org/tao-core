import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class CRS {

    public static void main(String[] args) throws FactoryException {
        final CoordinateReferenceSystem coordinateReferenceSystem = org.geotools.referencing.CRS.decode("EPSG:4326");
        System.out.println(coordinateReferenceSystem.getCoordinateSystem().getAxis(0).getAbbreviation());
    }
}
