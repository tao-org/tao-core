import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionTest {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        Connection connection = DriverManager.getConnection("jdbc:h2:./../db/taodata", "tao", "tao");
        System.exit(0);
    }
}
