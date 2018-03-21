package ro.cs.tao.persistence.convert;

import ro.cs.tao.eodata.enums.DataFormat;

import javax.persistence.AttributeConverter;

/**
 * Converter for DataFormat enum stored values
 *
 * @author oana
 *
 */
public class DataFormatConverter implements AttributeConverter<DataFormat, Integer> {

    @Override
    public Integer convertToDatabaseColumn(DataFormat attribute) {
        return attribute != null ? Integer.parseInt(attribute.toString()) : null;
    }

    @Override
    public DataFormat convertToEntityAttribute(Integer dbData) {
        return dbData != null ? DataFormat.valueOf(DataFormat.getEnumConstantNameByValue(dbData)) : null;
    }
}
