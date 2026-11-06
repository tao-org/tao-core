package ro.cs.tao.persistence.convert;

import jakarta.persistence.AttributeConverter;
import ro.cs.tao.component.enums.AuthenticationType;

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

