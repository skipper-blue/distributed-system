package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // XAMPP compatible configuration
    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String DATABASE = "drinks_system";
    private static final String USER = "root"; // XAMPP default user
    private static final String PASSWORD = ""; // XAMPP default (no password)
    
    // Alternative for custom user (uncomment to use)
    // private static final String USER = "drinks_user";
    // private static final String PASSWORD = "password";
    
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?useSSL=false&serverTimezone=UTC";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[DB] Connecting to: " + URL + " as " + USER);
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] ✓ Connection successful");
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] ✗ MySQL JDBC Driver not found. Make sure mysql-connector-java is in classpath.");
            throw new SQLException("MySQL Driver not found. Ensure mysql-connector-java JAR is in lib folder.", e);
        } catch (SQLException e) {
            System.err.println("[DB] ✗ Connection failed: " + e.getMessage());
            System.err.println("[DB] Check: 1) XAMPP MySQL is running, 2) Database 'drinks_system' exists, 3) Credentials correct");
            throw e;
        }
    }
}