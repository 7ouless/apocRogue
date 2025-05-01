package io.github.apocRogue.database;

import java.sql.*;

public class DBManager {
    private static final String URL = "jdbc:mysql://YOUR_HOST:3306/YOUR_DB?useSSL=false";
    private static final String USER = "dbuser";
    private static final String PASS = "dbpassword";

    private static Connection conn;

    // Call once at startup
    public static void connect() throws SQLException, ClassNotFoundException {
        if (conn == null || conn.isClosed()) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASS);
        }
    }

    public static boolean authenticate(String username, String password) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username=? AND password=SHA2(?,256)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 1;
            }
        }
    }

    public static boolean register(String username, String password) throws SQLException {
        //Checks if username exists
        String check = "SELECT COUNT(*) FROM users WHERE username=?";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) return false;  // already taken
            }
        }
        // Insert new user when registered
        String insert = "INSERT INTO users(username,password) VALUES(?, SHA2(?,256))";
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, username);
            ps.setString(2, password);
            return ps.executeUpdate() == 1;
        }
    }

    public static void close() {
        if (conn != null) try { conn.close(); } catch(Exception ignored){}
    }
}
