package ro.cs.tao.component.validation;

import ro.cs.tao.component.ParameterDescriptor;

import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Cosmin Cara
 */
@XmlTransient
public abstract class Validator {
    public abstract void validate(ParameterDescriptor parameter, Object value) throws ValidationException;
}
