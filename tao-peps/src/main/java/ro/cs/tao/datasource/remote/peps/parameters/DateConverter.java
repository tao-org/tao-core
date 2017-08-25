package ro.cs.tao.datasource.remote.peps.parameters;

import ro.cs.tao.datasource.converters.ConversionException;
import ro.cs.tao.datasource.converters.DefaultConverter;
import ro.cs.tao.datasource.param.QueryParameter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author Cosmin Cara
 */
public class DateConverter extends DefaultConverter {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private final DateTimeFormatter dateFormat;

    public DateConverter(QueryParameter parameter) {
        super(parameter);
        if (!Date.class.equals(parameter.getType())) {
            throw new IllegalArgumentException("Invalid parameter type");
        }
        dateFormat = DateTimeFormatter.ofPattern(DATE_FORMAT);
    }

    @Override
    public String stringValue() throws ConversionException {
        StringBuilder builder = new StringBuilder();
        LocalDateTime date = ((Date) parameter.getValue()).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        builder.append(date.format(dateFormat));
        return builder.toString();
    }
}