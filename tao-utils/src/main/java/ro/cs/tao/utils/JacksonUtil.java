package ro.cs.tao.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Jackson-based utility to allows us to easily transform JSON Object(s) to and from String
 *
 * @author Vlad Mihalcea (https://github.com/vladmihalcea/high-performance-java-persistence)
 *
 */
public final class JacksonUtil
{

    /**
     * ObjectMapper instance
     */
    public static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Default constructor
     */
    private JacksonUtil()
    {
        // empty constructor
    }

    /**
     * Deserialize JSON content from given JSON content String
     *
     * @param string - the string representation of JSON
     * @param clazz - the mapping object of JSON
     * @param <T> - type parameter substitute for JSON Object
     * @return JSON deserialized
     */
    public static <T> T fromString(final String string, final Class<T> clazz)
    {
        try
        {
            // ignore null objects or empty collections
            OBJECT_MAPPER = OBJECT_MAPPER.rebuild()
                    .changeDefaultPropertyInclusion(inclusion -> inclusion.withValueInclusion(Include.NON_NULL))
                    .changeDefaultPropertyInclusion(inclusion -> inclusion.withValueInclusion(Include.NON_EMPTY))
                    .build();

            return OBJECT_MAPPER.readValue(string, clazz);
        }
        catch (JacksonException e)
        {
            e.printStackTrace();
            throw new IllegalArgumentException("The given string value: " + string + " cannot be transformed to Json object");
        }
    }

    /**
     * Serialize JSON object to JSON String content
     *
     * @param value - the JSON object
     * @return - JSON String content
     */
    public static String toString(final Object value)
    {
        try
        {
            return OBJECT_MAPPER.writeValueAsString(value);
        }
        catch (JacksonException e)
        {
            e.printStackTrace();
            throw new IllegalArgumentException(
              "The given Json object value: " + value + " cannot be transformed to a String");
        }
    }

    /**
     * Deserialize JSON content as tree expressed using set of JsonNode instances
     * @param value - JSON content to parse to build the JSON tree.
     * @return the JSON tree node
     */
    public static JsonNode toJsonNode(final String value)
    {
        try
        {
            return OBJECT_MAPPER.readTree(value);
        }
        catch (JacksonException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Clone an object
     *
     * @param value - the source object
     * @param <T> - type parameter substitute for JSON Object
     * @return the cloned copy
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(final T value)
    {
        return fromString(toString(value), (Class<T>) value.getClass());
    }
}

