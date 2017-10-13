package ro.cs.tao.persistence.convert;

import ro.cs.tao.eodata.enums.DataFormat;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for DataFormat enum stored values
 *
 * @author oana
 *
 */
@Converter
public class DataFormatConverter implements AttributeConverter<DataFormat, Integer> {

    @Override
    public Integer convertToDatabaseColumn(DataFormat attribute) {

        return Integer.parseInt(attribute.toString());
    }

    @Override
    public DataFormat convertToEntityAttribute(Integer dbData) {
        return DataFormat.valueOf(DataFormat.getEnumConstantNameByValue(dbData));
    }
}
