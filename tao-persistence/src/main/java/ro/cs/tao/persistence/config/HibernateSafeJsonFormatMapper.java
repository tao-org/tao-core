package ro.cs.tao.persistence.config;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.FormatMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Avoids Jackson module auto-discovery (ServiceLoader) which can fail when
 * incompatible Jackson Hibernate modules are present on runtime classpath.
 */
public class HibernateSafeJsonFormatMapper implements FormatMapper {
    private final FormatMapper delegate;

    public HibernateSafeJsonFormatMapper() {
        this.delegate = createDelegate();
    }

    private FormatMapper createDelegate() {
        JsonMapper objectMapper = JsonMapper.builder().build();
        try {
            Class<?> mapperClass = Class.forName("org.hibernate.type.format.jackson.Jackson3JsonFormatMapper");
            return (FormatMapper) mapperClass.getConstructor(JsonMapper.class).newInstance(objectMapper);
        } catch (ReflectiveOperationException ignored) {
            try {
                Class<?> mapperClass = Class.forName("org.hibernate.type.format.jackson.JacksonJsonFormatMapper");
                return (FormatMapper) mapperClass.getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("No supported Hibernate JSON mapper found", e);
            }
        }
    }

    @Override
    public <T> T fromString(CharSequence charSequence, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        return delegate.fromString(charSequence, javaType, wrapperOptions);
    }

    @Override
    public <T> String toString(T value, JavaType<T> javaType, WrapperOptions wrapperOptions) {
        return delegate.toString(value, javaType, wrapperOptions);
    }
}
