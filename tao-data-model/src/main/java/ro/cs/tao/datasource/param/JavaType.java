package ro.cs.tao.datasource.param;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ro.cs.tao.TaoEnum;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.serialization.JavaTypeDeserializer;
import ro.cs.tao.utils.DateUtils;

import javax.xml.bind.annotation.XmlEnum;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.Function;

/**
 * JavaType is an enumeration representing different Java data types and their properties.
 * It provides functionalities for parsing, converting, and identifying supported Java types,
 * along with their descriptions and specific URNs (Uniform Resource Names).
 *
 * Each constant in the enumeration corresponds to a specific Java type, which includes
 * primitive types, arrays, strings, date-time, and other custom types.
 *
 * The constants in this enum are associated with:
 * - A `Class<?>` representing the Java type.
 * - A human-readable description of the type.
 * - An OGC (Open Geospatial Consortium) URN identifying the type conceptually.
 * - A converter function to transform a string into an instance of the corresponding type.
 *
 * This enumeration also provides utility methods to facilitate interactions with the enumerated
 * constants, such as parsing values, identifying types based on name or class, and creating arrays
 * of a given type and dimension.
 */
@XmlEnum(Class.class)
@JsonDeserialize(using = JavaTypeDeserializer.class)
public enum JavaType implements TaoEnum<Class<?>> {

    /**
     * Represents the Java primitive data type {@code byte}.
     * This type corresponds to a signed integer with a width of 8 bits (1 byte).
     * It supports the parsing of values represented as strings into {@code byte}
     * instances using the associated converter function.
     */
    BYTE(byte.class, "byte", "urn:ogc:def:positiveInteger", Byte::parseByte),
    /**
     * Represents the JavaType for an array of bytes. This type is associated with the `byte[]` class
     * and is identified by a friendly name "byte[]". It supports parsing and converting string values
     * into their respective byte array representations using the specified converter function.
     */
    BYTE_ARRAY(byte[].class, "byte[]", "urn:ogc:def:positiveIntegerList", Byte::parseByte),
    /**
     * Represents the Java primitive type {@code short}. This constant is part of the {@code JavaType} enumerator
     * and defines characteristics and handling of the {@code short} data type.
     */
    SHORT(short.class, "short", "urn:ogc:def:integer", Short::parseShort),
    /**
     * Represents a Java type corresponding to an array of primitive short values.
     * This type provides metadata and utility methods for handling short arrays
     * in context of OGC (Open Geospatial Consortium) specifications.
     */
    SHORT_ARRAY(short[].class, "short[]", "urn:ogc:def:integerList", Short::parseShort),
    /**
     * Represents the Java type for primitive integer values (`int`).
     * This is defined with its corresponding Java class type, a description string,
     * its OGC (Open Geospatial Consortium) URN, and a converter function for string-to-integer parsing.
     */
    INT(int.class, "int", "urn:ogc:def:integer", Integer::parseInt),
    /**
     * Represents a Java type that corresponds to an array of integers.
     * This type is associated with the `int[]` class and provides mechanisms
     * for parsing and handling integer array values.
     */
    INT_ARRAY(int[].class, "int[]", "urn:ogc:def:integerList", Integer::parseInt),
    /**
     * Represents the Java primitive `long` type and provides metadata and utility methods for
     * parsing and type-specific operations. This enumeration constant is associated with the `long`
     * type, identified by its class representation, descriptive name, OGC URN, and a converter function.
     */
    LONG(long.class, "long", "urn:ogc:def:integer", Long::parseLong),
    /**
     * Represents a type definition for arrays of primitive long values.
     * This type encapsulates the class type, a human-readable description,
     * an OGC (Open Geospatial Consortium) URN, and a converter for parsing
     * string representations of long values into their respective object form.
     */
    LONG_ARRAY(long[].class, "long[]", "urn:ogc:def:integerList", Long::parseLong),
    /**
     * Represents the Java type associated with the primitive {@code float} data type.
     * This type descriptor is used to map data to the {@code float} type in various operations,
     * including parsing and validation.
     */
    FLOAT(float.class, "float", "urn:ogc:def:scale", Float::parseFloat),
    /**
     * Represents the {@code float[]} type in the {@code JavaType} enumeration.
     *
     * This constant is used to define a float array type and provides the following details:
     */
    FLOAT_ARRAY(float[].class, "float[]", "urn:ogc:def:scaleList", Float::parseFloat),
    /**
     * Represents the `double` type within the `JavaType` enumeration.
     * This constant is used to handle Java's double primitive type and supports
     * conversion from string values to double instances using a specified converter.
     */
    DOUBLE(double.class, "double", "urn:ogc:def:scale", Double::parseDouble),
    /**
     * Represents the Java type corresponding to a primitive double array.
     */
    DOUBLE_ARRAY(double[].class, "double[]", "urn:ogc:def:scaleList", Double::parseDouble),
    /**
     * Represents the {@code JavaType} for a single String value.
     * This type is associated with the {@code String} class and provides a mechanism to parse Strings
     * as Java objects using the associated converter function.
     */
    STRING(String.class, "string", "urn:ogc:def:string", s -> s),
    /**
     * Represents the Java type associated with an array of strings.
     * This type encapsulates the class representation, a description,
     * the OGC (Open Geospatial Consortium) URN, and a converter function
     * used for processing string representations of this type.
     */
    STRING_ARRAY(String[].class, "string[]", "urn:ogc:def:string", s -> s),
    /**
     * Represents a Java type corresponding to a {@link LocalDateTime}.
     * This type is used to handle date-time values in a consistent manner,
     * utilizing a specific converter function for parsing string representations
     * of dates into {@link LocalDateTime} objects.
     */
    DATE(LocalDateTime.class, "date", "urn:ogc:def:time", DateUtils::parseDate),
    /**
     * Represents an array of date-time values with a defined type, description, and parsing functionality.
     * This type is intended to handle lists of date-time representations and provides support
     * for type-safe parsing of date-time lists based on a specific format.
     */
    DATE_ARRAY(LocalDateTime[].class, "date[]", "urn:ogc:def:timeList", DateUtils::parseDate),
    /**
     * Represents the Java type corresponding to the {@code boolean} primitive type.
     * This type is used for handling boolean values within the system, supporting parsing
     * and conversions from string representations.
     */
    BOOLEAN(boolean.class, "bool", "urn:ogc:def:boolean", Boolean::parseBoolean),
    /**
     * Represents the type corresponding to a boolean array in the {@code JavaType} enumeration.
     * Provides functionality for parsing and handling boolean array values.
     *
     * This type is associated with the boolean array Java class ({@code boolean[].class}),
     * a friendly name of "bool[]", an associated OGC (Open Geospatial Consortium) URN,
     * and a converter function for transforming string representations into boolean values.
     */
    BOOLEAN_ARRAY(boolean[].class, "bool[]", "\"urn:ogc:def:string", Boolean::parseBoolean),
    /**
     * Represents a Java type corresponding to a 2D polygon structure.
     * This type uses a {@link Polygon2D} class to define its structure and operations.
     * It supports conversion from WKT (Well-Known Text) format to create polygon instances.
     */
    POLYGON(Polygon2D.class, "polygon", "urn:ogc:def:anyURI", Polygon2D::fromWKT),
    /**
     * Specifies a Java type representation for paths.
     *
     * This constant defines a type that represents file system paths using the {@link Path} class.
     */
    PATH(Path.class, "path", "urn:ogc:def:string", Path::of),
    /**
     * Represents the "PATH_ARRAY" JavaType, which is designed to handle an array of Path objects.
     * This type uses a converter function to transform string representations into Path instances.
     */
    PATH_ARRAY(Path[].class, "path[]", "urn:ogc:def:stringList", Path::of),
    /**
     * Represents a Well-Known Text (WKT) geometry type used for spatial data representation.
     * WKT defines spatial features such as points, lines, and polygons in a text-based format.
     * This type is specifically associated with the "wkt" description and the OGC URN "urn:ogc:def:string".
     *
     * The value of the WKT is processed using a converter function, allowing string inputs to
     * be transformed into the appropriate WKT object representation.
     */
    WKT(WKT.class, "wkt", "urn:ogc:def:string", WKT::new),
    /**
     * Represents an array type for Well-Known Text (WKT) strings.
     * This type is associated with the WKT class and provides an implementation
     * to handle arrays of WKT objects, including parsing and mapping from string representations.
     *
     * The converter function specified for this type maps a string input to a new
     * WKT instance and is primarily used for transforming string inputs into WKT objects.
     */
    WKT_ARRAY(WKT[].class, "wkt[]", "urn:ogc:def:stringList", WKT::new);

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

    /**
     * Retrieves the OGC (Open Geospatial Consortium) URN (Uniform Resource Name) associated with this object.
     *
     * @return The OGC URN string.
     */
    public String ogcURN() { return this.ogcURN; }

    /**
     * Retrieves the converter function associated with this instance. The converter
     * is typically used to transform a string*/
    public Function<String, Object> getConverter() { return this.converter; }

    /**
     * Parses the provided string value into an object of the expected type using the configured converter.
     * If the value is null, the method returns null cast to the appropriate type.
     * If the class type is an array, this method will throw an {@link IllegalArgumentException}.
     *
     * @param value The string value to be parsed. Can be null.
     * @return The parsed object of the expected type, or {@code null} if the input value is null.
     * @throws IllegalArgumentException If the method is called on an array type.
     */
    public Object parse(String value) {
        if (this.clazz.isArray()) {
            throw new IllegalArgumentException("Method should not be called on an array type");
        }
        return value != null ? this.converter.apply(value) : this.clazz.cast(null);
    }

    /**
     * Determines if the current class type represented by this instance is an array type.
     *
     * @return {@code true} if the class type is an array, {@code false} otherwise.
     */
    public boolean isArrayType() { return this.clazz.isArray(); }

    /**
     * Parses the provided string value into an array of the expected type using the configured converter.
     * The method expects the input string to represent an array format (e.g., "[value1,value2,...]").
     * If the input is null, the method returns null cast to the array type.
     * If the type represented by the class is not an array, an {@link IllegalArgumentException} is thrown.
     *
     * @param value The string value to be parsed as an array. Can be null. If non-null, it should represent
     *              an array in string form, with elements separated by commas and enclosed in brackets.
     * @return An array of the expected type with the parsed elements, or {@code null} if the input value is null.
     * @throws IllegalArgumentException If the class type is not an array or if the value does not represent a valid array.
     */
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
            Object array = Array.newInstance(this.clazz.getComponentType(), split.length);
            for (int i = 0; i < split.length; i++) {
                Array.set(array, i, this.converter.apply(split[i]));
            }
            return array;
        }
    }

    /**
     * Retrieves the corresponding {@code JavaType} enum instance for the given class.
     * If no matching enum instance is found, the method defaults to returning {@code STRING}.
     *
     * @param clazz The class for which to determine the corresponding {@code JavaType}.
     *              Must not be {@code null}.
     * @return The {@code JavaType} instance associated with the given class,
     *         or {@code STRING} if no matching type is found.
     */
    public static JavaType fromClass(Class<?> clazz) {
        for (JavaType type : JavaType.values()) {
            if (type.clazz.equals(clazz)) {
                return type;
            }
        }
        return STRING;
    }

    /**
     * Retrieves the corresponding {@code JavaType} enum instance matching the given class name.
     * If the class name cannot be resolved or no matching type is found, the method defaults to returning {@code STRING}.
     *
     * @param className The fully qualified name of the class to match with a {@code JavaType} instance.
     *                  Must not be {@code null}.
     * @return The {@code JavaType} instance corresponding to the given class name, or {@code STRING} if no match is found
     *         or the class cannot be resolved.
     */
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

    /**
     * Retrieves a {@code JavaType} instance by matching the provided friendly name or class name.
     * If no match is found, this method defaults to returning the {@code STRING} type.
     *
     * @param name The friendly name or class name to be matched. Must not be {@code null}.
     * @return The corresponding {@code JavaType} instance if a match is found, otherwise {@code STRING}.
     */
    public static JavaType fromFriendlyName(String name) {
        for (JavaType type : JavaType.values()) {
            if (type.description.equals(name) || type.clazz.getName().equals(name)) {
                return type;
            }
        }
        return STRING;
    }

    /**
     * Checks if the provided name corresponds to the description of any of the available {@code JavaType} instances.
     *
     * @param name The name to validate against the descriptions of {@code JavaType} instances.
     *             Must not be {@code null}.
     * @return {@code true} if the provided name matches the description of any {@code JavaType} instance;
     *         {@code false} otherwise.
     */
    public static boolean isValid(String name) {
        return Arrays.stream(JavaType.values()).anyMatch(t -> t.description.equals(name));
    }

    /**
     * Creates and returns an array of the specified type and dimension.
     * The method validates the provided type and dimension before creating the array.
     *
     * @param type The {@code JavaType} representing the type of the array to be created.
     *             Must be a valid array type (e.g., INT_ARRAY, STRING_ARRAY).
     * @param dimension The size of the array to be created. Must be greater than 0.
     * @return An array of the specified type and dimension.
     * @throws IllegalArgumentException If the provided dimension is less than or equal to 0,
     *                                  or if the specified type is not an array type.
     */
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
            case PATH_ARRAY:
                array = new Path[dimension];
                break;
            case WKT_ARRAY:
                array = new WKT[dimension];
                break;
        }
        return array;
    }
}
