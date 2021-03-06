/**
 * Copyright (C) 2015 Johannes Schnatterer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.schnatterer.songbirddbapi4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;

/**
 * Abstracts from the Songbird database database file. Make sure to call
 * {@link #close()} after using it.
 * 
 * <b>This class is not thread safe.</b> Use a new instance in each thread.
 * 
 * @author schnatterer
 * 
 */
public final class SongbirdDbConnection {

	/**
	 * @param pathToDb
	 *            the dbUrl to set
	 */
	public SongbirdDbConnection(final String pathToDb) {
		this.dbUrl = JDBC_PREFIX + pathToDb;
	}

	/** SLF4J-Logger. */
	private final Logger logger = LoggerFactory
			.getLogger(SongbirdDbConnection.class);

	/** The jdbc prefix used for connecting. */
	private static final String JDBC_PREFIX = "jdbc:sqlite:";

	/** The string representation of the JDBC driver used for connecting. */
	private static final String JDBC_DRIVER = "org.sqlite.JDBC";

	/** Connection timeout during the execution of a statement. */
	private static final int STATEMENT_TIMEOUT = 30;

	/** Actual database URL to connect to. */
	private String dbUrl = null;

	/** Connection to the database. */
	private Connection connection = null;

	/** Closes the database connection. */
	public void close() {
		try {
			close(connection);
		} finally {
			connection = null;
		}
	}

	/** Closes the database connection. */
	private void close(Connection actualConnection) {
		try {
			if (actualConnection != null) {
				logger.debug("Closing connection to " + dbUrl);
				actualConnection.close();
			}
		} catch (SQLException e) {
			// connection close failed.
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Returns a connection to the songbird database.
	 * 
	 * @return a connection ready for queries to the songbird database
	 * @throws SQLException
	 *             in case of any problems (e.g. SQLite-related or missing
	 *             database URL)
	 */
	private Connection getConnection() throws SQLException {
		if (connection == null) {
			/* See: http://www.xerial.org/trac/Xerial/wiki/SQLiteJDBC */

			if (dbUrl == null) {
				throw new SQLException("Missing URL to songbird db");
			}

			// Load the sqlite-JDBC driver using the current class loader
			try {
				Class.forName(JDBC_DRIVER);
				// Class.forName("org.sqlite.Driver");

			} catch (ClassNotFoundException e) {
				throw new SQLException("SQLlite driver not found on classpath",
						e);
			}
			SQLiteConfig config = new SQLiteConfig();
			config.setReadOnly(true);
			config.setSharedCache(true);

			// create a database connection
			logger.info("Opening connection to " + dbUrl);
			connection = config.createConnection(dbUrl);
			// logger.debug(String.format("running in %s mode",
			// org.sqlite.SQLiteJDBCLoader.isNativeMode() ? "native"
			// : "pure-java"));

			// connection = DriverManager.getConnection(dbUrl);
			// Create custom songbird collations (see DatabaseEngine.cpp)
			// createCollations((JdbcConnection) connection);
		}
		return connection;
	}

	/**
	 * Create a statement using the {@link #connection}.
	 * 
	 * @return a new statement object.
	 * @throws SQLException
	 *             if a database access error occurs or this method is called on
	 *             a closed connection
	 */
	private Statement getStatment() throws SQLException {
		Connection actualConnection = getConnection();
		Statement statement;
		statement = actualConnection.createStatement();
		statement.setQueryTimeout(STATEMENT_TIMEOUT);
		return statement;
	}

	/**
	 * Executes an SQL query as a new Statement.
	 * 
	 * @param query
	 *            an SQL statement to be sent to the database, typically a
	 *            static SQL SELECT statement
	 * 
	 * @return a ResultSet object that contains the data produced by the given
	 *         query; never null
	 * @throws SQLException
	 *             if a database access error occurs, this method is called on a
	 *             closed Statement, the given SQL statement produces anything
	 *             other than a single ResultSet object, the method is called on
	 *             a PreparedStatement or CallableStatement
	 */
	public ResultSet executeQuery(final String query) throws SQLException {
		// logger.debug("Query to SQLite: " + query);
		return getStatment().executeQuery(query);
	}

	/**
	 * Creates a PreparedStatement object for sending parameterized SQL
	 * statements to the database.
	 * 
	 * @param query
	 *            an SQL statement that may contain one or more '?' IN parameter
	 *            placeholders
	 * 
	 * @return new default PreparedStatement object containing the pre-compiled
	 *         SQL statement
	 * 
	 * @throws SQLException
	 *             if a database access error occurs or this method is called on
	 *             a closed connection
	 */
	public PreparedStatement preparedStatement(final String query)
			throws SQLException {
		return getConnection().prepareStatement(query);
	}

	/**
	 * @return the dbUrl
	 */
	public String getDbUrl() {
		return dbUrl;
	}
}
