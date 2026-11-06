package ro.cs.tao.serialization;

import ro.cs.tao.datasource.param.JavaType;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * An abstract mix-in class intended to provide Jackson serialization customization
 * for fields of type {@link JavaType}.
 *
 * This mix-in class employs a custom serializer {@link JavaTypeSerializer} to handle
 * serialization of the associated {@code dataType} field, altering the way the
 * {@code dataType} is serialized into JSON representation.
 *
 * The {@code JavaType} is expected to be transformed using the logic defined
 * in the {@link JavaTypeSerializer} implementation.
 *
 * @author  Cosmin Cara
 */
public abstract class DataTypeMixIn {
    @JsonSerialize(using = JavaTypeSerializer.class)
    public JavaType dataType;
}
