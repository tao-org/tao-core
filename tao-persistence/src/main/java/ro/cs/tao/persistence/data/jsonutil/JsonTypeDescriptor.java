package ro.cs.tao.persistence.data.jsonutil;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.MutableMutabilityPlan;
import org.hibernate.usertype.DynamicParameterizedType;

import java.util.Properties;

/**
 * JSON data type descriptor responsible for transforming the JSON Object type used in the entity mapping classes
 * to a format that is supported by the underlying database.
 *
 * @author Vlad Mihalcea (https://github.com/vladmihalcea/high-performance-java-persistence)
 *
 */
public class JsonTypeDescriptor extends AbstractTypeDescriptor<Object> implements DynamicParameterizedType
{

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 472690702608240906L;

    /**
     * Java Class for JSON object
     */
    private Class< ? > jsonObjectClass;

    /**
     * Default constructor
     */
    @SuppressWarnings("serial")
    public JsonTypeDescriptor()
    {
        //call the super class
        super(Object.class, new MutableMutabilityPlan<Object>()
        {
            @Override
            protected Object deepCopyNotNull(final Object value)
            {
                //clone value
                return JacksonUtil.clone(value);
            }
        });
    }

    @Override
    public void setParameterValues(final Properties parameters)
    {
        //get json object class
        jsonObjectClass = ((ParameterType) parameters.get(PARAMETER_TYPE)).getReturnedClass();

    }

    @Override
    public boolean areEqual(final Object one, final Object another)
    {
        if (one == another)
        {
            //are equals
            return true;
        }
        if (one == null || another == null)
        {
            //are not equals
            return false;
        }
        return JacksonUtil.toJsonNode(JacksonUtil.toString(one))
          .equals(JacksonUtil.toJsonNode(JacksonUtil.toString(another)));
    }

    @Override
    public String toString(final Object value)
    {
        //Serialize JSON object
        return JacksonUtil.toString(value);
    }

    @Override
    public Object fromString(final String string)
    {
        //Deserialize JSON content
        return JacksonUtil.fromString(string, jsonObjectClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X> X unwrap(final Object value, final Class<X> type, final WrapperOptions options)
    {
        if (value == null)
        {
            return null;
        }
        if (String.class.isAssignableFrom(type))
        {
            return (X) toString(value);
        }
        if (Object.class.isAssignableFrom(type))
        {
            return (X) JacksonUtil.toJsonNode(toString(value));
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> Object wrap(final X value, final WrapperOptions options)
    {
        if (value == null)
        {
            return null;
        }
        return fromString(value.toString());
    }

}

