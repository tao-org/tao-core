package ro.cs.tao.component.converters;

import ro.cs.tao.datasource.converters.ConversionException;

/**
 * @author Cosmin Cara
 */
public class LongConverter extends DefaultConverter<Long> {

    @Override
    public Long fromString(String value) throws ConversionException {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new ConversionException(ex.getMessage());
        }
    }
}
