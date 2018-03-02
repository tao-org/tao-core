package ro.cs.tao.persistence.data.jsonutil;

import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.BasicExtractor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Abstract SQL type descriptor for JSON data type
 * Base class that allows us to notify Hibernate that the expected JDBC type is not a SQL-standard column type,
 * as well as the ResultSet extraction logic
 *
 * @author Vlad Mihalcea (https://github.com/vladmihalcea/high-performance-java-persistence)
 *
 */
public abstract class AbstractJsonSqlTypeDescriptor implements SqlTypeDescriptor
{

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 7946559840963265798L;

    @Override
    public int getSqlType()
    {
        //SQL type is database specific
        return Types.OTHER;
    }

    @Override
    public boolean canBeRemapped()
    {
        //this descriptor is available for remapping
        return true;
    }

    @Override
    public <X> ValueExtractor<X> getExtractor(final JavaTypeDescriptor<X> javaTypeDescriptor)
    {
        return new BasicExtractor<X>(javaTypeDescriptor, this)
        {
            @Override
            protected X doExtract(final ResultSet rs, final String name, final WrapperOptions options) throws SQLException
            {
                //wrap the value as our handled Java type
                return javaTypeDescriptor.wrap(rs.getObject(name), options);
            }

            @Override
            protected X doExtract(final CallableStatement statement, final int index, final WrapperOptions options) throws SQLException
            {
                //wrap the value as our handled Java type
                return javaTypeDescriptor.wrap(statement.getObject(index), options);
            }

            @Override
            protected X doExtract(final CallableStatement statement, final String name, final WrapperOptions options) throws SQLException
            {
                //wrap the value as our handled Java type
                return javaTypeDescriptor.wrap(statement.getObject(name), options);
            }
        };
    }

}
