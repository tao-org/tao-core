package ro.cs.tao.persistence.convert;

import jakarta.persistence.AttributeConverter;
import ro.cs.tao.utils.JacksonUtil;
import ro.cs.tao.workspaces.SiteAdministrativeArea;

public class SiteAdministrativeAreaConverter implements AttributeConverter<SiteAdministrativeArea, String> {

    @Override
    public String convertToDatabaseColumn(SiteAdministrativeArea attribute) {
        return attribute == null || attribute.isEmpty() ? null : JacksonUtil.toString(attribute);
    }

    @Override
    public SiteAdministrativeArea convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.trim().isEmpty()
                ? null
                : JacksonUtil.fromString(dbData, SiteAdministrativeArea.class);
    }
}
