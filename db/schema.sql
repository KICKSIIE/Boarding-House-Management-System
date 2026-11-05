CREATE DATABASE IF NOT EXISTS boarding_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE boarding_system;
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    UNIQUE KEY users_email_unique (email),
    UNIQUE KEY users_username_unique (username)
);
