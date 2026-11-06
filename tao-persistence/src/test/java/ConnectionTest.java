import org.junit.Test;
import ro.cs.tao.utils.Assert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class ConnectionTest {

    @Test
    public void testConnection() throws ClassNotFoundException, SQLException{
        Class.forName("org.h2.Driver");
        Connection connection = DriverManager.getConnection("jdbc:h2:./../db/taodata", "tao", "tao");
        Assert.notNull(connection);
    }

    @Test
    public void resetDatabase() throws ClassNotFoundException, SQLException{ // The db will be created in /tao-core/db
        Class.forName("org.h2.Driver");
        Connection connection = DriverManager.getConnection("jdbc:h2:./../db/taodata;MODE=PostgreSQL;INIT=RUNSCRIPT FROM 'classpath:schema.sql'\\;RUNSCRIPT FROM 'classpath:data.sql'", "tao", "tao");
        Assert.notNull(connection);
    }

}
