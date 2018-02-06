package ro.cs.tao.component.converters;

import ro.cs.tao.datasource.converters.ConversionException;

/**
 * @author Cosmin Cara
 */
public class BooleanConverter extends DefaultConverter<Boolean> {
    @Override
    public Boolean fromString(String value) throws ConversionException {
        return Boolean.parseBoolean(value);
    }
}
