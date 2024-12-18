package ro.cs.tao.serialization;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ro.cs.tao.datasource.param.JavaType;

public abstract class DataTypeMixIn {
    @JsonSerialize(using = JavaTypeSerializer.class)
    public JavaType dataType;
}
