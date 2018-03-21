package ro.cs.tao.persistence.convert;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Converter for List<String> stored values
 *
 * @author oana
 */
public class ConstraintsConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        String result = null;

        if (attribute != null && attribute.size() > 0) {
            result = "";
            for (String constraint : attribute) {
                result = result.concat(constraint).concat(";");
            }
            result = result.substring(0, result.lastIndexOf(";"));
        }

        return result;
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        List<String> result = null;

        if (!StringUtils.isEmpty(dbData)) {
            result = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(dbData, ";");
            while (st.hasMoreElements()) {
                result.add(st.nextToken());
            }
        }

        return result;
    }
}
