package ro.cs.tao.datasource.param;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ro.cs.tao.TaoEnum;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.serialization.JavaTypeDeserializer;
import ro.cs.tao.utils.DateUtils;

import javax.xml.bind.annotation.XmlEnum;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.function.Function;

@XmlEnum(Class.class)
@JsonDeserialize(using = JavaTypeDeserializer.class)
public enum JavaType implements TaoEnum<Class<?>> {
    BYTE(byte.class, "byte", "urn:ogc:def:positiveInteger", Byte::parseByte),
    BYTE_ARRAY(byte[].class, "byte[]", "urn:ogc:def:positiveIntegerList", Byte::parseByte),
    SHORT(short.class, "short", "urn:ogc:def:integer", Short::parseShort),
    SHORT_ARRAY(short[].class, "short[]", "urn:ogc:def:integerList", Short::parseShort),
    INT(int.class, "int", "urn:ogc:def:integer", Integer::parseInt),
    INT_ARRAY(int[].class, "int[]", "urn:ogc:def:integerList", Integer::parseInt),
    LONG(long.class, "long", "urn:ogc:def:integer", Long::parseLong),
    LONG_ARRAY(long[].class, "long[]", "urn:ogc:def:integerList", Long::parseLong),
    FLOAT(float.class, "float", "urn:ogc:def:scale", Float::parseFloat),
    FLOAT_ARRAY(float[].class, "float[]", "urn:ogc:def:scaleList", Float::parseFloat),
    DOUBLE(double.class, "double", "urn:ogc:def:scale", Double::parseDouble),
    DOUBLE_ARRAY(double[].class, "double[]", "urn:ogc:def:scaleList", Double::parseDouble),
    STRING(String.class, "string", "urn:ogc:def:string", s -> s),
    STRING_ARRAY(String[].class, "string[]", "urn:ogc:def:string", s -> s),
    DATE(LocalDateTime.class, "date", "urn:ogc:def:time", DateUtils::parseDate),
    DATE_ARRAY(LocalDateTime[].class, "date[]", "urn:ogc:def:timeList", DateUtils::parseDate),
    BOOLEAN(boolean.class, "bool", "urn:ogc:def:boolean", Boolean::parseBoolean),
    BOOLEAN_ARRAY(boolean[].class, "bool[]", "\"urn:ogc:def:string", Boolean::parseBoolean),
    POLYGON(Polygon2D.class, "polygon", "urn:ogc:def:anyURI", Polygon2D::fromWKT);

    private final Class<?> clazz;
    private final String description;
    private final String ogcURN;
    private final Function<String, Object> converter;

    JavaType(Class<?> paramClass, String description, String ogcURN, Function<String, Object> converter) {
        this.clazz = paramClass;
        this.description = description;
        this.ogcURN = ogcURN;
        this.converter = converter;
    }

    @Override
    public String friendlyName() {
        return this.description;
    }

    @Override
    public Class<?> value() {
        return this.clazz;
    }

    public String ogcURN() { return this.ogcURN; }

    public Object parse(String value) {
        if (this.clazz.isArray()) {
            throw new IllegalArgumentException("Method should not be called on an array type");
        }
        return value != null ? this.converter.apply(value) : this.clazz.cast(null);
    }

    public boolean isArrayType() { return this.clazz.isArray(); }

    public Object parseArray(String value) {
        if (!this.clazz.isArray()) {
            throw new IllegalArgumentException("Method should be called on an array type");
        }
        if (value == null) {
            return this.clazz.cast(null);
        } else {
            if (!value.startsWith("[") && !value.endsWith("]")) {
                throw new IllegalArgumentException("Value does not represent an array");
            }
            String[] split = value.substring(1, value.length() - 1).split(",");
            Object array = Array.newInstance(this.clazz, split.length);
            for (int i = 0; i < split.length; i++) {
                Array.set(array, i, this.converter.apply(split[i]));
            }
            return array;
        }
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

    public static JavaType fromFriendlyName(String name) {
        for (JavaType type : JavaType.values()) {
            if (type.description.equals(name) || type.clazz.getName().equals(name)) {
                return type;
            }
        }
        return STRING;
    }

    public static Object createArray(JavaType type, int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("[dimension]");
        }
        if (!type.description.endsWith("[]")) {
            throw new IllegalArgumentException("[type] not an array type");
        }
        Object array = null;
        switch (type) {
            case BYTE_ARRAY:
                array = new byte[dimension];
                break;
            case SHORT_ARRAY:
                array = new short[dimension];
                break;
            case INT_ARRAY:
                array = new int[dimension];
                break;
            case LONG_ARRAY:
                array = new long[dimension];
                break;
            case FLOAT_ARRAY:
                array = new float[dimension];
                break;
            case DOUBLE_ARRAY:
                array = new double[dimension];
                break;
            case STRING_ARRAY:
                array = new String[dimension];
                break;
        }
        return array;
    }
}
