package ro.cs.tao.datasource.param;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ro.cs.tao.TaoEnum;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.serialization.JavaTypeDeserializer;

import javax.xml.bind.annotation.XmlEnum;
import java.time.LocalDateTime;

@XmlEnum(Class.class)
@JsonDeserialize(using = JavaTypeDeserializer.class)
public enum JavaType implements TaoEnum<Class<?>> {
    BYTE(byte.class, "byte"),
    BYTE_ARRAY(byte[].class, "byte[]"),
    SHORT(short.class, "short"),
    SHORT_ARRAY(short[].class, "short[]"),
    INT(int.class, "int"),
    INT_ARRAY(int[].class, "int[]"),
    LONG(long.class, "long"),
    LONG_ARRAY(long[].class, "long[]"),
    FLOAT(float.class, "float"),
    FLOAT_ARRAY(float[].class, "float[]"),
    DOUBLE(double.class, "double"),
    DOUBLE_ARRAY(double[].class, "double[]"),
    STRING(String.class, "string"),
    STRING_ARRAY(String[].class, "string[]"),
    DATE(LocalDateTime.class, "date"),
    DATE_ARRAY(LocalDateTime[].class, "date[]"),
    BOOLEAN(boolean.class, "bool"),
    BOOLEAN_ARRAY(boolean[].class, "bool[]"),
    POLYGON(Polygon2D.class, "polygon"),
    DATA_FORMAT(DataFormat.class, "format"),
    SENSOR_TYPE(SensorType.class, "sensor");

    private final Class<?> clazz;
    private final String description;

    JavaType(Class<?> paramClass, String description) {
        this.clazz = paramClass;
        this.description = description;
    }

    @Override
    public String friendlyName() {
        return this.description;
    }

    @Override
    public Class<?> value() {
        return this.clazz;
    }

    public static JavaType fromClass(Class<?> clazz) {
        for (JavaType type : JavaType.values()) {
            if (type.clazz.equals(clazz)) {
                return type;
            }
        }
        return STRING;
    }

    public static JavaType fromClassName(String className) {
        final Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        for (JavaType type : JavaType.values()) {
            if (type.clazz.equals(clazz)) {
                return type;
            }
        }
        return STRING;
    }
}
