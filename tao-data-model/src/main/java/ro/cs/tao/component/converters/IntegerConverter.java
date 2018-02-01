package ro.cs.tao.component.converters;

import ro.cs.tao.datasource.converters.ConversionException;

/**
 * @author Cosmin Cara
 */
public class IntegerConverter extends DefaultConverter<Integer> {

    @Override
    public Integer fromString(String value) throws ConversionException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new ConversionException(ex.getMessage());
        }
    }
}
