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

        return Integer.parseInt(attribute.toString());
    }

    @Override
    public DataFormat convertToEntityAttribute(Integer dbData) {
        return DataFormat.valueOf(DataFormat.getEnumConstantNameByValue(dbData));
    }
}
