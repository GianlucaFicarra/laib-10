package it.polito.tdp.porto.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.DataSources;

public class DBConnect {

	private static String jdbcURL = "jdbc:mysql://localhost/porto2015?user=root&password=gualtieri95";
	private static DataSource ds;

	public static Connection getConnection() {

		if (ds == null) {
			// crea il DataSource
			try {
				ds = DataSources.pooledDataSource(
						DataSources.unpooledDataSource(jdbcURL));
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		try {
			Connection c = ds.getConnection();
			return c;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
	
	
	/*
	 * import java.sql.Connection;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnect {

	private static String jdbcURL = "jdbc:mysql://localhost/porto2015";
	private static HikariDataSource ds;
	
	public static Connection getConnection() {

		if (ds == null) {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(jdbcURL);
			config.setUsername("root");
			config.setPassword("root");
			
			// configurazione MySQL
			config.addDataSourceProperty("cachePrepStmts", "true");
			config.addDataSourceProperty("prepStmtCacheSize", "250");
			config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
			
			ds = new HikariDataSource(config);
		}
		
		try {
			Connection c = ds.getConnection();
			return c;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
	  */
	 

}