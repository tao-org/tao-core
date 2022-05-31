package ro.cs.tao.datasource.param;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ro.cs.tao.TaoEnum;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.serialization.JavaTypeDeserializer;

import javax.xml.bind.annotation.XmlEnum;
import java.time.LocalDateTime;

@XmlEnum(Class.class)
@JsonDeserialize(using = JavaTypeDeserializer.class)
public enum JavaType implements TaoEnum<Class<?>> {
    BYTE(byte.class, "byte", "urn:ogc:def:positiveInteger"),
    BYTE_ARRAY(byte[].class, "byte[]", "urn:ogc:def:positiveIntegerList"),
    SHORT(short.class, "short", "urn:ogc:def:integer"),
    SHORT_ARRAY(short[].class, "short[]", "urn:ogc:def:integerList"),
    INT(int.class, "int", "urn:ogc:def:integer"),
    INT_ARRAY(int[].class, "int[]", "urn:ogc:def:integerList"),
    LONG(long.class, "long", "urn:ogc:def:integer"),
    LONG_ARRAY(long[].class, "long[]", "urn:ogc:def:integerList"),
    FLOAT(float.class, "float", "urn:ogc:def:scale"),
    FLOAT_ARRAY(float[].class, "float[]", "urn:ogc:def:scaleList"),
    DOUBLE(double.class, "double", "urn:ogc:def:scale"),
    DOUBLE_ARRAY(double[].class, "double[]", "urn:ogc:def:scaleList"),
    STRING(String.class, "string", "urn:ogc:def:string"),
    STRING_ARRAY(String[].class, "string[]", "urn:ogc:def:string"),
    DATE(LocalDateTime.class, "date", "urn:ogc:def:time"),
    DATE_ARRAY(LocalDateTime[].class, "date[]", "urn:ogc:def:timeList"),
    BOOLEAN(boolean.class, "bool", "urn:ogc:def:boolean"),
    BOOLEAN_ARRAY(boolean[].class, "bool[]", "\"urn:ogc:def:string"),
    POLYGON(Polygon2D.class, "polygon", "urn:ogc:def:anyURI");

    private final Class<?> clazz;
    private final String description;
    private final String ogcURN;

    JavaType(Class<?> paramClass, String description, String ogcURN) {
        this.clazz = paramClass;
        this.description = description;
        this.ogcURN = ogcURN;
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
}
