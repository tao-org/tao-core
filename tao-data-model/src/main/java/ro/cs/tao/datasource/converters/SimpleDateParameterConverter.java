package ro.cs.tao.datasource.converters;

import ro.cs.tao.datasource.param.QueryParameter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimpleDateParameterConverter extends DefaultParameterConverter<LocalDateTime> {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    protected DateTimeFormatter dateFormat;

    public SimpleDateParameterConverter(QueryParameter<LocalDateTime> parameter) {
        super(parameter);
        if (!LocalDateTime.class.equals(parameter.getType())) {
            throw new IllegalArgumentException("Invalid parameter type");
        }
        dateFormat = DateTimeFormatter.ofPattern(DATE_FORMAT);
    }

    @Override
    public String stringValue() throws ConversionException {
        StringBuilder builder = new StringBuilder();
        LocalDateTime minValue, maxValue = null;
        if (parameter.isInterval()) {
            minValue = parameter.getMinValue();
            maxValue = parameter.getMaxValue();
        } else {
            minValue = parameter.getValue();
        }
        if (minValue != null) {
            builder.append(minValue.format(dateFormat));
        }
        if (maxValue != null) {
            builder.append(" TO ");
            builder.append(maxValue.format(dateFormat));
        }
        return builder.toString();
    }
}

