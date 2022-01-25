package ro.cs.tao.persistence.convert;

import ro.cs.tao.component.WPSAuthentication;
import ro.cs.tao.utils.JacksonUtil;

import javax.persistence.AttributeConverter;

public class AuthenticationTypeConverter implements AttributeConverter<WPSAuthentication, String> {
    @Override
    public String convertToDatabaseColumn(WPSAuthentication wpsAuthentication) {
        return wpsAuthentication != null ? JacksonUtil.toString(wpsAuthentication) : null;
    }

    @Override
    public WPSAuthentication convertToEntityAttribute(String value) {
        return value != null ? JacksonUtil.fromString(value, WPSAuthentication.class) : null;
    }
}
