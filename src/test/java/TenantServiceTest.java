package com.example.systempractice;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantServiceTest {
    private static final String URL = "jdbc:h2:mem:tenants;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private final TenantService service = new TenantService(() -> DriverManager.getConnection(URL));

    @BeforeEach void setup() throws SQLException {
        try (Connection c = DriverManager.getConnection(URL)) {
            var s = c.createStatement();
            s.execute("DROP ALL OBJECTS");
            s.execute("CREATE TABLE users (email VARCHAR(254) PRIMARY KEY)");
            s.execute("CREATE TABLE rooms (id BIGINT PRIMARY KEY, room_number VARCHAR(50), description VARCHAR(500), monthly_rent DECIMAL(10,2))");
            s.execute("CREATE TABLE tenant_rooms (user_email VARCHAR(254) PRIMARY KEY REFERENCES users(email), room_id BIGINT REFERENCES rooms(id))");
            s.execute("CREATE TABLE rent_payments (id BIGINT AUTO_INCREMENT PRIMARY KEY, user_email VARCHAR(254) REFERENCES users(email), amount DECIMAL(10,2), paid_on DATE, note VARCHAR(500))");
            s.execute("CREATE TABLE maintenance_requests (id BIGINT AUTO_INCREMENT PRIMARY KEY, user_email VARCHAR(254) REFERENCES users(email), description VARCHAR(1000), status VARCHAR(30) DEFAULT 'Open', created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            s.execute("INSERT INTO users VALUES ('one@example.com'), ('two@example.com')");
            s.execute("INSERT INTO rooms VALUES (1, '101', 'First floor', 250.00)");
            s.execute("INSERT INTO tenant_rooms VALUES ('one@example.com', 1)");
            s.execute("INSERT INTO rent_payments (user_email, amount, paid_on, note) VALUES ('one@example.com', 200.00, '2026-01-01', 'January'), ('two@example.com', 300.00, '2026-02-01', '')");
        }
    }

    @Test void tenantSeesOnlyOwnRecords() throws Exception {
        assertEquals("101", service.room("one@example.com").orElseThrow().number());
        assertTrue(service.room("two@example.com").isEmpty());
        assertEquals("January", service.payments("one@example.com").get(0).note());
        assertEquals(1, service.payments("two@example.com").size());
        service.submitRequest("one@example.com", "  Leaking tap  ");
        assertEquals("Leaking tap", service.requests("one@example.com").get(0).description());
        assertEquals("Open", service.requests("one@example.com").get(0).status());
        assertTrue(service.requests("two@example.com").isEmpty());
        assertThrows(SQLException.class, () -> service.submitRequest("missing@example.com", "Problem"));
    }

    @Test void invalidRequestsAreRejected() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> service.submitRequest("one@example.com", "   "));
        assertThrows(IllegalArgumentException.class, () -> service.submitRequest("one@example.com", "x".repeat(1001)));
        assertTrue(service.requests("one@example.com").isEmpty());
    }
}
