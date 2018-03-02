package ro.cs.tao.persistence.data.jsonutil;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.usertype.DynamicParameterizedType;

import java.util.Properties;

/**
 * Data type for sending a JSON Object to the database using a String
 *
 * @author Vlad Mihalcea (https://github.com/vladmihalcea/high-performance-java-persistence)
 *
 */
public class JsonStringType extends AbstractSingleColumnStandardBasicType<Object> implements DynamicParameterizedType
{

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -7381626711164828174L;

    /**
     * Default constructor
     */
    public JsonStringType()
    {
        super(JsonStringSqlTypeDescriptor.INSTANCE, new JsonTypeDescriptor());
    }

    public String getName()
    {
        return "json";
    }

    @Override
    protected boolean registerUnderJavaType()
    {
        return true;
    }

    @Override
    public void setParameterValues(final Properties parameters)
    {
        ((JsonTypeDescriptor) getJavaTypeDescriptor()).setParameterValues(parameters);
    }

}

