package com.onlineexam.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private DBConnection() {
    }

    private static final String URL = "jdbc:mysql://localhost:3307/onlineexam";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {

        try {
            // Nạp MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "Khong tim thay MySQL JDBC Driver", e);
        }

        return DriverManager.getConnection(
                URL, USER, PASSWORD);
    }
}