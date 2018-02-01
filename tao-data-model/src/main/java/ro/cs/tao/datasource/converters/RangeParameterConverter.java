package ro.cs.tao.datasource.converters;

import ro.cs.tao.datasource.param.QueryParameter;

/**
 * @author Cosmin Cara
 */
public class RangeParameterConverter extends DefaultParameterConverter {

    public RangeParameterConverter(QueryParameter parameter) {
        super(parameter);
        if (!Number.class.isAssignableFrom(parameter.getType()) &&
                !parameter.isInterval()) {
            throw new IllegalArgumentException("Invalid parameter type");
        }
    }

    @Override
    public String stringValue() throws ConversionException {
        StringBuilder builder = new StringBuilder();
        Number minValue = (Number) this.parameter.getMinValue();
        if (minValue == null) {
            minValue = 0;
        }
        builder.append("[").append(minValue).append(",");
        Number maxValue = (Number) this.parameter.getMaxValue();
        if (maxValue == null) {
            maxValue = 100;
        }
        builder.append(maxValue).append("]");
        return builder.toString();
    }
}
