package ro.cs.tao.component.converters;

import ro.cs.tao.datasource.converters.ConversionException;

/**
 * @author Cosmin Cara
 */
public class ByteConverter extends DefaultConverter<Byte> {
    @Override
    public Byte fromString(String value) throws ConversionException {
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException ex) {
            throw new ConversionException(ex.getMessage());
        }
    }
}
