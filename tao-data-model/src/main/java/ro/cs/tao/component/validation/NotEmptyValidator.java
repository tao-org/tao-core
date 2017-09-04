package ro.cs.tao.component.validation;

import ro.cs.tao.component.ParameterDescriptor;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Cosmin Cara
 */
@XmlRootElement
public class NotEmptyValidator extends Validator {

    NotEmptyValidator() { }

    @Override
    public void validate(ParameterDescriptor parameter, Object value) throws ValidationException {
        if (parameter == null) {
            throw new ValidationException("Cannot validate a null reference");
        }
        if (value.toString().trim().isEmpty()) {
            throw new ValidationException(String.format("Value for [%s] must not be empty.", parameter.getId()));
        }
    }
}
