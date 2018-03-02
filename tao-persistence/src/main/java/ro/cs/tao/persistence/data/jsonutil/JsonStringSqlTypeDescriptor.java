package ro.cs.tao.persistence.data.jsonutil;

import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.BasicBinder;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * SQL type descriptor for JSON data type
 *
 * @author Vlad Mihalcea (https://github.com/vladmihalcea/high-performance-java-persistence)
 *
 */
public class JsonStringSqlTypeDescriptor extends AbstractJsonSqlTypeDescriptor
{

    /**
     * JsonStringSqlTypeDescriptor instance
     */
    public static final JsonStringSqlTypeDescriptor INSTANCE = new JsonStringSqlTypeDescriptor();

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -3055574760500416999L;

    /**
     * Default constructor
     */
    public JsonStringSqlTypeDescriptor()
    {
        // empty constructor
    }

    @Override
    public <X> ValueBinder<X> getBinder(final JavaTypeDescriptor<X> javaTypeDescriptor)
    {
        return new BasicBinder<X>(javaTypeDescriptor, this)
        {
            @Override
            protected void doBind(final PreparedStatement st, final X value, final int index, final WrapperOptions options) throws SQLException
            {
                st.setString(index, javaTypeDescriptor.unwrap(value, String.class, options));
            }

            @Override
            protected void doBind(final CallableStatement st, final X value, final String name, final WrapperOptions options) throws SQLException
            {
                st.setString(name, javaTypeDescriptor.unwrap(value, String.class, options));
            }
        };
    }
}

