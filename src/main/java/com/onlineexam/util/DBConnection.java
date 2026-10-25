package com.onlineexam.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * SQL Server connection helper.
 *
 * Set these environment variables before running:
 * ONLINEEXAM_DB_URL, ONLINEEXAM_DB_USER, ONLINEEXAM_DB_PASSWORD
 * Do not commit real credentials to GitHub.
 */
public final class DBConnection {
    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getenv("ONLINEEXAM_DB_URL");
        String user = System.getenv("ONLINEEXAM_DB_USER");
        String password = System.getenv("ONLINEEXAM_DB_PASSWORD");

        if (url == null || user == null || password == null) {
            throw new SQLException(
                    "Database settings are missing. Set ONLINEEXAM_DB_URL, ONLINEEXAM_DB_USER, ONLINEEXAM_DB_PASSWORD.");
        }

        return DriverManager.getConnection(url, user, password);
    }
}
