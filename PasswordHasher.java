package com.example.systempractice;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {

    public static String hashPassword(String password) {
        try {
            // Using SHA-256 hash function
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));  // Convert byte to hex format
            }

            return hexString.toString(); // Return hashed password
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null; // Return null if hashing fails
        }
    }
}



