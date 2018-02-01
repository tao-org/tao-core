package ro.cs.tao.component.converters;

import ro.cs.tao.datasource.converters.ConversionException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Cosmin Cara
 */
public class DateConverter extends DefaultConverter<Date> {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Date fromString(String value) throws ConversionException {
        try {
            return format.parse(value);
        } catch (ParseException e) {
            throw new ConversionException(e.getMessage());
        }
    }

    @Override
    public String stringValue(Date value) throws ConversionException {
        try {
            return format.format(value);
        } catch (Exception ex) {
            throw new ConversionException(ex.getMessage());
        }
    }
}
