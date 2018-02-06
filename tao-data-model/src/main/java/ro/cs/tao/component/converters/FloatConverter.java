package ro.cs.tao.component.converters;

import ro.cs.tao.datasource.converters.ConversionException;

/**
 * @author Cosmin Cara
 */
public class FloatConverter extends DefaultConverter<Float> {
    @Override
    public Float fromString(String value) throws ConversionException {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ex) {
            throw new ConversionException(ex.getMessage());
        }
    }
}
