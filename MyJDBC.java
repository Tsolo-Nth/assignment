package com.example.vehicle;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class MyJDBC {
    //  Use correct JDBC port (3306) for MySQL (not 33060)
    private static final String URL = "jdbc:mysql://localhost:3306/vehicle?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection connection = null;
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                //  Load JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                //  Connect to MySQL
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println(" Connected to MySQL successfully!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println(" MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println(" Database connection failed!");
            e.printStackTrace();
        }
        return connection;
    }
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println(" Connection closed successfully.");
            } catch (SQLException e) {
                System.err.println(" Failed to close the connection!");
            }
        }
        e.printStackTrace();
    }
}