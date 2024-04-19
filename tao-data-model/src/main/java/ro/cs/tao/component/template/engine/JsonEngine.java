package ro.cs.tao.component.template.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.template.TemplateException;
import ro.cs.tao.component.template.TemplateType;
import ro.cs.tao.datasource.param.JavaType;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.utils.DateUtils;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;

public class JsonEngine extends TemplateEngine {
    private final ObjectMapper mapper;
    private final Function<Object, String> numericToString = String::valueOf;
    private final Function<Object, String> dateToString = value -> "\"" + ((LocalDateTime) value).format(DateUtils.getResilientFormatterAtUTC()) + "\"";
    private final Function<Object, String> objectToString = o -> "\"" + o + "\"";
    private final Function<Object, String> polygonToString = o -> ((Polygon2D) o).toWKT();

    public JsonEngine() {
        this.mapper = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        this.mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public TemplateType getTemplateType() {
        return TemplateType.JSON;
    }

    @Override
    public void parse(Template template) throws TemplateException {
        parse(template.getContents());
    }

    @Override
    public void parse(String text) throws TemplateException {
        try {
            this.mapper.readTree(text);
        } catch (JsonProcessingException e) {
            throw new TemplateException(e);
        }
    }

    @Override
    public String transform(Template template, Map<String, Object> parameters) throws TemplateException {
        String contents = template.getContents();
        if (parameters != null) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                final Object value = entry.getValue();
                final String key = entry.getKey();
                if (value != null) {
                    JavaType type = JavaType.fromClass(value.getClass());
                    switch (type) {
                        case BOOLEAN:
                        case BYTE:
                        case SHORT:
                        case INT:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                            contents = contents.replace("\"$" + key + "\"", numericToString.apply(value));
                            break;
                        case DATE:
                            contents = contents.replace("\"$" + key + "\"", dateToString.apply(value));
                            break;
                        case STRING:
                            contents = contents.replace("\"$" + key + "\"", objectToString.apply(value));
                            break;
                        case POLYGON:
                            contents = contents.replace("$" + key, polygonToString.apply(value));
                            break;
                        case BOOLEAN_ARRAY:
                        case BYTE_ARRAY:
                        case SHORT_ARRAY:
                        case INT_ARRAY:
                        case LONG_ARRAY:
                        case FLOAT_ARRAY:
                        case DOUBLE_ARRAY:
                            contents = contents.replace("\"$" + key + "\"", buildArray(value, numericToString));
                            break;
                        case DATE_ARRAY:
                            contents = contents.replace("\"$" + key + "\"", buildArray(value, dateToString));
                            break;
                        case STRING_ARRAY:
                            contents = contents.replace("\"$" + key + "\"", buildArray(value, objectToString));
                            break;
                    }
                } else {
                    contents = contents.replace("\"$" + key + "\"", "null");
                }
            }
        }
        return contents;
    }

    private String buildArray(Object array, Function<Object, String> valueToStringFunction) {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        final int size = Array.getLength(array);
        for (int i = 0; i < size; i++) {
            builder.append(valueToStringFunction.apply(Array.get(array, i))).append(",");
        }
        builder.setLength(builder.length() - 1);
        builder.append("]");
        return builder.toString();
    }
}
