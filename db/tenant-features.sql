-- Run after the account schema (and upgrade-existing.sql for older installations).
-- Existing users.email must have the same type, character set and collation as
-- the user_email columns below; check your existing schema before applying.
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL DEFAULT '',
    monthly_rent DECIMAL(10,2) NOT NULL,
    CHECK (monthly_rent >= 0)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tenant_rooms (
    user_email VARCHAR(254) NOT NULL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    FOREIGN KEY (user_email) REFERENCES users(email),
    FOREIGN KEY (room_id) REFERENCES rooms(id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS rent_payments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(254) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    paid_on DATE NOT NULL,
    note VARCHAR(500) NOT NULL DEFAULT '',
    FOREIGN KEY (user_email) REFERENCES users(email),
    CHECK (amount > 0)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS maintenance_requests (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(254) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'Open',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_email) REFERENCES users(email)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
