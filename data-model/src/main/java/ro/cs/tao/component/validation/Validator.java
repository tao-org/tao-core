package ro.cs.tao.component.validation;

import ro.cs.tao.component.ParameterDescriptor;

/**
 * @author Cosmin Cara
 */
public interface Validator {
    void validate(ParameterDescriptor parameter, Object value) throws ValidationException;
}
