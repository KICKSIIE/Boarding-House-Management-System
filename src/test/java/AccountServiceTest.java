package com.example.systempractice;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountServiceTest {
    private static final String URL = "jdbc:h2:mem:accounts;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private final AccountService accounts = new AccountService(() -> DriverManager.getConnection(URL));

    @BeforeEach void setup() throws Exception {
        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.createStatement().execute("DROP TABLE IF EXISTS users");
            conn.createStatement().execute("CREATE TABLE users (email VARCHAR(254) NOT NULL UNIQUE, username VARCHAR(100) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL)");
        }
    }

    @Test void signupAndLogin() throws Exception {
        assertEquals(AccountService.SignupResult.CREATED, accounts.signup(" user@example.com ", " user ", "password123"));
        assertTrue(accounts.login("user@example.com", "password123"));
        assertFalse(accounts.login("user@example.com", "wrongpassword"));
        assertFalse(accounts.login("missing@example.com", "password123"));
        assertEquals(AccountService.SignupResult.EMAIL_TAKEN, accounts.signup("user@example.com", "other", "password123"));
        assertEquals(AccountService.SignupResult.USERNAME_TAKEN, accounts.signup("other@example.com", "user", "password123"));
        try (Connection conn = DriverManager.getConnection(URL); ResultSet rs = conn.createStatement().executeQuery("SELECT password FROM users")) {
            assertTrue(rs.next()); assertTrue(rs.getString(1).startsWith("pbkdf2-sha256$"));
        }
    }

    @Test void validatesBeforeDatabaseAccess() throws Exception {
        assertEquals(AccountService.SignupResult.INVALID_EMAIL, accounts.signup("@", "user", "password123"));
        assertEquals(AccountService.SignupResult.INVALID_USERNAME, accounts.signup("user@example.com", "  ", "password123"));
        assertEquals(AccountService.SignupResult.INVALID_PASSWORD, accounts.signup("user@example.com", "user", ""));
    }

    @Test void oldPasswordsAreUpgradedOnlyAfterSuccessfulLogin() throws Exception {
        String legacy = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8";
        try (Connection conn = DriverManager.getConnection(URL); PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (?, ?, ?)")) {
            stmt.setString(1, "legacy@example.com"); stmt.setString(2, "legacy"); stmt.setString(3, legacy); stmt.executeUpdate();
        }
        assertFalse(accounts.login("legacy@example.com", "wrong"));
        try (Connection conn = DriverManager.getConnection(URL); ResultSet rs = conn.createStatement().executeQuery("SELECT password FROM users")) {
            assertTrue(rs.next()); assertEquals(legacy, rs.getString(1));
        }
        assertTrue(accounts.login("legacy@example.com", "password"));
        try (Connection conn = DriverManager.getConnection(URL); ResultSet rs = conn.createStatement().executeQuery("SELECT password FROM users")) {
            assertTrue(rs.next()); assertTrue(rs.getString(1).startsWith("pbkdf2-sha256$"));
        }
        assertTrue(accounts.login("legacy@example.com", "password"));
    }

    @Test void saltedHashesAndMalformedValues() {
        String one = PasswordHasher.hashPassword("secret");
        String two = PasswordHasher.hashPassword("secret");
        assertNotEquals(one, two);
        assertTrue(PasswordHasher.verifyPassword("secret", one));
        assertFalse(PasswordHasher.verifyPassword("incorrect", one));
        assertFalse(PasswordHasher.verifyPassword("secret", "pbkdf2-sha256$999999999$bad$bad"));
    }
}
