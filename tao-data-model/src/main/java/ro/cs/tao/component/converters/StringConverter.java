package ro.cs.tao.component.converters;

import ro.cs.tao.datasource.converters.ConversionException;

/**
 * @author Cosmin Cara
 */
public class StringConverter extends DefaultConverter<String> {

    @Override
    public String stringValue(String value) throws ConversionException {
        return value;
    }

    @Override
    public String fromString(String value) throws ConversionException {
        return value;
    }
}
