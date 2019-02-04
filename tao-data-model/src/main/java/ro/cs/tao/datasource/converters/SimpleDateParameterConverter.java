package ro.cs.tao.datasource.converters;

import ro.cs.tao.datasource.param.QueryParameter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class SimpleDateParameterConverter extends DefaultParameterConverter {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    protected DateTimeFormatter dateFormat;

    public SimpleDateParameterConverter(QueryParameter parameter) {
        super(parameter);
        if (!Date.class.equals(parameter.getType())) {
            throw new IllegalArgumentException("Invalid parameter type");
        }
        dateFormat = DateTimeFormatter.ofPattern(DATE_FORMAT);
    }

    @Override
    public String stringValue() throws ConversionException {
        StringBuilder builder = new StringBuilder();
        Object minValue = null, maxValue = null;
        if (parameter.isInterval()) {
            minValue = parameter.getMinValue();
            maxValue = parameter.getMaxValue();
        } else {
            minValue = parameter.getValue();
        }
        if (minValue != null) {
            LocalDateTime minDate = ((Date) minValue).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            builder.append(minDate.format(dateFormat));
        }
        if (maxValue != null) {
            builder.append(" TO ");
            LocalDateTime maxDate = ((Date) maxValue).toInstant()
                    .atZone((ZoneId.systemDefault())).toLocalDateTime();
            builder.append(maxDate.format(dateFormat));
        }
        return builder.toString();
    }
}

