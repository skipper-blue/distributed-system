package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String LOCAL_MYSQL_HOST = "localhost";
    private static final int LOCAL_MYSQL_PORT = 3306;
    private static final String LOCAL_MYSQL_DATABASE = "drinks_system";
    private static final String LOCAL_MYSQL_USER = "root";
    private static final String LOCAL_MYSQL_PASSWORD = "";

    private static final String ENV_DB_URL = System.getenv("DB_URL");
    private static final String ENV_DB_USER = System.getenv("DB_USER");
    private static final String ENV_DB_PASS = System.getenv("DB_PASS");

    private static final String DEFAULT_URL = "jdbc:mysql://" + LOCAL_MYSQL_HOST + ":" + LOCAL_MYSQL_PORT + "/" + LOCAL_MYSQL_DATABASE + "?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = LOCAL_MYSQL_USER;
    private static final String DEFAULT_PASSWORD = LOCAL_MYSQL_PASSWORD;

    public static Connection getConnection() throws SQLException {
        String url = ENV_DB_URL != null && !ENV_DB_URL.isBlank() ? ENV_DB_URL : DEFAULT_URL;
        String user = ENV_DB_USER != null && !ENV_DB_USER.isBlank() ? ENV_DB_USER : DEFAULT_USER;
        String password = ENV_DB_PASS != null ? ENV_DB_PASS : DEFAULT_PASSWORD;

        try {
            if (url.startsWith("jdbc:postgresql:")) {
                Class.forName("org.postgresql.Driver");
            } else if (url.startsWith("jdbc:mysql:")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            }

            System.out.println("[DB] Connecting to: " + url + " as " + user);
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("[DB] ✓ Connection successful");
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] ✗ JDBC Driver not found for URL: " + url);
            throw new SQLException("JDBC Driver not found for URL: " + url, e);
        } catch (SQLException e) {
            System.err.println("[DB] ✗ Connection failed: " + e.getMessage());
            System.err.println("[DB] Check DB_URL, DB_USER, DB_PASS and database availability.");
            throw e;
        }
    }
}