package ro.cs.tao.persistence.convert;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import ro.cs.tao.serialization.CRSAdapter;

@Converter(autoApply = true)
public class CRSConverter implements AttributeConverter<CoordinateReferenceSystem, String> {

    private final CRSAdapter adapter = new CRSAdapter();

    @Override
    public String convertToDatabaseColumn(CoordinateReferenceSystem attribute) {
        try {
            return attribute != null ? adapter.unmarshal(attribute) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public CoordinateReferenceSystem convertToEntityAttribute(String dbData) {
        try {
            return dbData != null ? adapter.marshal(dbData) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
