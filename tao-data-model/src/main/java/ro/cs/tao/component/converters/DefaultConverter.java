package ro.cs.tao.component.converters;

import ro.cs.tao.datasource.converters.ConversionException;

/**
 * @author Cosmin Cara
 */
public class DefaultConverter<T> implements ParameterConverter<T> {
    @Override
    public String stringValue(T value) throws ConversionException {
        return String.valueOf(value);
    }
}
