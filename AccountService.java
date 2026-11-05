package com.example.systempractice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountService {
    @FunctionalInterface public interface ConnectionFactory { Connection open() throws SQLException; }
    private final ConnectionFactory connections;
    public AccountService(ConnectionFactory connections) { this.connections = connections; }

    public enum SignupResult { CREATED, INVALID_EMAIL, INVALID_USERNAME, INVALID_PASSWORD, USERNAME_TAKEN, EMAIL_TAKEN }

    public SignupResult signup(String email, String username, String password) throws SQLException {
        email = email.trim();
        username = username.trim();
        if (email.length() > 254 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return SignupResult.INVALID_EMAIL;
        if (username.isEmpty() || username.length() > 100) return SignupResult.INVALID_USERNAME;
        if (password.length() < 8 || password.length() > 256) return SignupResult.INVALID_PASSWORD;
        try (Connection conn = connections.open()) {
            SignupResult existing = existingAccount(conn, email, username);
            if (existing != null) return existing;
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (email, username, password) VALUES (?, ?, ?)")) {
                stmt.setString(1, email); stmt.setString(2, username); stmt.setString(3, PasswordHasher.hashPassword(password));
                stmt.executeUpdate();
                return SignupResult.CREATED;
            } catch (SQLException e) {
                if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                    existing = existingAccount(conn, email, username);
                    if (existing != null) return existing;
                }
                throw e;
            }
        }
    }

    private SignupResult existingAccount(Connection conn, String email, String username) throws SQLException {
        if (exists(conn, "SELECT 1 FROM users WHERE email = ?", email)) return SignupResult.EMAIL_TAKEN;
        if (exists(conn, "SELECT 1 FROM users WHERE username = ?", username)) return SignupResult.USERNAME_TAKEN;
        return null;
    }

    private boolean exists(Connection conn, String query, String value) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next(); }
        }
    }

    public boolean login(String email, String password) throws SQLException {
        try (Connection conn = connections.open(); PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE email = ?")) {
            stmt.setString(1, email.trim());
            String stored;
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return false;
                stored = rs.getString("password");
            }
            if (!PasswordHasher.verifyPassword(password, stored)) return false;
            if (PasswordHasher.needsUpgrade(stored)) {
                try (PreparedStatement update = conn.prepareStatement("UPDATE users SET password = ? WHERE email = ? AND password = ?")) {
                    update.setString(1, PasswordHasher.hashPassword(password)); update.setString(2, email.trim()); update.setString(3, stored);
                    if (update.executeUpdate() != 1) return false;
                }
            }
            return true;
        }
    }
}
