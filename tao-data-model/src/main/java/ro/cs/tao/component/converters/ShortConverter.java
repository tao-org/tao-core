package ro.cs.tao.component.converters;

import ro.cs.tao.datasource.converters.ConversionException;

/**
 * @author Cosmin Cara
 */
public class ShortConverter extends DefaultConverter<Short> {
    @Override
    public Short fromString(String value) throws ConversionException {
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException ex) {
            throw new ConversionException(ex.getMessage());
        }
    }
}
