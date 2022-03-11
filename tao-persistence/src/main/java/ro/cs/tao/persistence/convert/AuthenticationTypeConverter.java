package ro.cs.tao.persistence.convert;

import ro.cs.tao.component.enums.AuthenticationType;

import javax.persistence.AttributeConverter;

public class AuthenticationTypeConverter implements AttributeConverter<AuthenticationType, String> {
    @Override
    public String convertToDatabaseColumn(AuthenticationType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public AuthenticationType convertToEntityAttribute(String value) {
        return value != null ? AuthenticationType.valueOf(value) : null;
    }
}
