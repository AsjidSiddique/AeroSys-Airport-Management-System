package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public final class DatabaseConnection {

    // ── Connection parameters ─────────────────────────────────────────
    private static final String URL  =
            "jdbc:mysql://127.0.0.1:3306/aerosys_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "0469";

    // ── Load driver once at class initialisation ──────────────────────
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Hard failure: the JAR is missing from the classpath.
            // Wrap as RuntimeException so the developer sees it immediately.
            throw new RuntimeException(
                    "MySQL JDBC driver not found on classpath. "
                  + "Add mysql-connector-j-*.jar to your project libraries.", e);
        }
    }

    // ── Public factory method ─────────────────────────────────────────

      public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // ── Prevent instantiation ─────────────────────────────────────────
    private DatabaseConnection() {}
}
