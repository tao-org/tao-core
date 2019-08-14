/**
 * 
 */
package ro.cs.tao.scheduling;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.quartz.utils.ConnectionProvider;

/**
 * Quartz Connection provider over an existing connection.
 * 
 * @author Lucian Barbulescu
 *
 */
public class SharedConnectionProvider implements ConnectionProvider {

	/**
	 * THe existing data source
	 */
	private final DataSource dataSource;
	
	/**
	 * Constructor.
	 * 
	 * @param dataSource the existing data source
	 */
	public SharedConnectionProvider(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	@Override
	public void shutdown() throws SQLException {
		// the data source will be closed elsewhere
		
	}

	@Override
	public void initialize() throws SQLException {
		// Nothing to do as the connection is already initialised
		
	}

}
