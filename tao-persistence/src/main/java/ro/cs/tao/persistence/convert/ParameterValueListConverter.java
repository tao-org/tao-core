package ro.cs.tao.persistence.convert;


import ro.cs.tao.persistence.data.jsonutil.JacksonUtil;
import ro.cs.tao.workflow.ParameterValue;

import javax.persistence.AttributeConverter;
import java.util.List;

public class ParameterValueListConverter  implements AttributeConverter<List<ParameterValue>, String>
{

    @Override
    public String convertToDatabaseColumn(List<ParameterValue> attribute) {
        return JacksonUtil.toString(attribute);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ParameterValue> convertToEntityAttribute(String dbData) {

        return (List<ParameterValue>) JacksonUtil.fromString(dbData, List.class);
    }
}
