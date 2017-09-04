package ro.cs.tao.component.validation;

import ro.cs.tao.component.ParameterDescriptor;

import java.util.List;

/**
 * @author Cosmin Cara
 */
public class CompositeValidator extends Validator {
    private List<Validator> internalValidators;

    public CompositeValidator(List<Validator> validators) {
        this.internalValidators = validators;
    }

    @Override
    public void validate(ParameterDescriptor parameter, Object value) throws ValidationException {
        if (this.internalValidators != null) {
            for (Validator validator : this.internalValidators) {
                validator.validate(parameter, value);
            }
        }
    }
}
