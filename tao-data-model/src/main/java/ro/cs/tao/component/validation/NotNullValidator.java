package ro.cs.tao.component.validation;

import ro.cs.tao.component.ParameterDescriptor;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Cosmin Cara
 */
@XmlRootElement
public class NotNullValidator extends Validator {

    NotNullValidator() { }

    @Override
    public void validate(ParameterDescriptor parameter, Object value) throws ValidationException {
        if (parameter == null) {
            throw new ValidationException("Cannot validate a null reference");
        }
        if (parameter.isNotNull() && value == null) {
            throw new ValidationException(String.format("Value for [%s] must be not null.", parameter.getId()));
        }
    }
}
