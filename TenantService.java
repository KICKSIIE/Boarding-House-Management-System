package com.example.systempractice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TenantService {
    public record Room(String number, String description, BigDecimal monthlyRent) { }
    public record Payment(BigDecimal amount, LocalDate paidOn, String note) { }
    public record Request(String description, String status, LocalDateTime createdAt) { }
    private final AccountService.ConnectionFactory connections;
    public TenantService(AccountService.ConnectionFactory connections) { this.connections = connections; }

    public Optional<Room> room(String email) throws SQLException {
        String sql = "SELECT r.room_number, r.description, r.monthly_rent FROM rooms r JOIN tenant_rooms t ON t.room_id = r.id WHERE t.user_email = ?";
        try (Connection conn = connections.open(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(new Room(rs.getString(1), rs.getString(2), rs.getBigDecimal(3))) : Optional.empty();
            }
        }
    }

    public List<Payment> payments(String email) throws SQLException {
        List<Payment> result = new ArrayList<>();
        try (Connection conn = connections.open(); PreparedStatement stmt = conn.prepareStatement("SELECT amount, paid_on, note FROM rent_payments WHERE user_email = ? ORDER BY paid_on DESC, id DESC")) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(new Payment(rs.getBigDecimal(1), rs.getDate(2).toLocalDate(), rs.getString(3)));
            }
        }
        return result;
    }

    public List<Request> requests(String email) throws SQLException {
        List<Request> result = new ArrayList<>();
        try (Connection conn = connections.open(); PreparedStatement stmt = conn.prepareStatement("SELECT description, status, created_at FROM maintenance_requests WHERE user_email = ? ORDER BY created_at DESC, id DESC")) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(new Request(rs.getString(1), rs.getString(2), rs.getTimestamp(3).toLocalDateTime()));
            }
        }
        return result;
    }

    public void submitRequest(String email, String description) throws SQLException {
        String value = description.trim();
        if (value.isEmpty() || value.length() > 1000) throw new IllegalArgumentException("Enter a request of up to 1,000 characters.");
        try (Connection conn = connections.open(); PreparedStatement stmt = conn.prepareStatement("INSERT INTO maintenance_requests (user_email, description) VALUES (?, ?)")) {
            stmt.setString(1, email); stmt.setString(2, value); stmt.executeUpdate();
        }
    }
}
