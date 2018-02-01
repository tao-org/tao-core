package ro.cs.tao.component.converters;

import ro.cs.tao.datasource.converters.ConversionException;

/**
 * @author Cosmin Cara
 */
public class DoubleConverter extends DefaultConverter<Double> {
    @Override
    public Double fromString(String value) throws ConversionException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            throw new ConversionException(ex.getMessage());
        }
    }
}
