package com.example.vehicle;

import java.sql.Connection;
public class TestDatabaseConnection {
    public static void main(String[] args) {
        //  Test MySQL database connection
        Connection conn = MyJDBC.getConnection();
        if (conn != null) {
            System.out.println(" Connection established successfully.");
            MyJDBC.closeConnection();
        } else {
            System.out.println(" Failed to establish connection.");
        }
    }
}
