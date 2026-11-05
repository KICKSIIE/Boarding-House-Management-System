package com.example.systempractice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class database_server {
    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("BOARDING_DB_URL", "jdbc:mysql://localhost:3306/boarding_system");
        String user = System.getenv("BOARDING_DB_USER");
        String password = System.getenv("BOARDING_DB_PASSWORD");
        if (user == null || user.isBlank() || password == null) {
            throw new SQLException("Database credentials are not configured.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
