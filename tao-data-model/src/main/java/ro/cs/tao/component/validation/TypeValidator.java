package ro.cs.tao.component.validation;

import ro.cs.tao.component.ParameterDescriptor;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Cosmin Cara
 */
@XmlRootElement
public class TypeValidator extends Validator {

    TypeValidator() { }

    @Override
    public void validate(ParameterDescriptor parameter, Object value) throws ValidationException {
        if (parameter == null) {
            throw new ValidationException("Cannot validate a null reference");
        }
        final Class<?> dataType = parameter.getDataType();
        if (!isAssignableFrom(dataType, value)) {
            throw new ValidationException(String.format("Value for [%s] must be of type %s",
                                                        parameter.getId(),
                                                        dataType.getSimpleName()));
        }
    }

    private boolean isAssignableFrom(Class<?> type, Object value) {
        if (value == null) {
            return !type.isPrimitive();
        }
        final Class<?> valueType = value.getClass();
        return type.isAssignableFrom(valueType)
                || type.isPrimitive()
                && (type.equals(Boolean.TYPE) && valueType.equals(Boolean.class)
                || type.equals(Character.TYPE) && valueType.equals(Character.class)
                || type.equals(Byte.TYPE) && valueType.equals(Byte.class)
                || type.equals(Short.TYPE) && valueType.equals(Short.class)
                || type.equals(Integer.TYPE) && valueType.equals(Integer.class)
                || type.equals(Long.TYPE) && valueType.equals(Long.class)
                || type.equals(Float.TYPE) && valueType.equals(Float.class)
                || type.equals(Double.TYPE) && valueType.equals(Double.class));
    }
}
