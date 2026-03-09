package com.hobby.shop.security.crypto.bcrypt;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class VerifyPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // The hash from your database
        String storedHash = "$2a$10$8K9p4YQnL3xZxZxZxZxZuOe9QZDhxz73hJPaEv7/cBha4pk0AgP.";

        // Test with correct password
        String correctPassword = "password123";
        boolean correctMatch = encoder.matches(correctPassword, storedHash);
        System.out.println("Correct password matches: " + correctMatch);

        // Test with wrong password
        String wrongPassword = "wrongpassword";
        boolean wrongMatch = encoder.matches(wrongPassword, storedHash);
        System.out.println("Wrong password matches: " + wrongMatch);

        // Show what a fresh hash looks like
        String freshHash = encoder.encode("password123");
        System.out.println("\nFresh hash of 'password123': " + freshHash);
        System.out.println("Fresh hash length: " + freshHash.length());

        // Compare hashes
        System.out.println("\nStored hash length: " + storedHash.length());
        System.out.println("Stored hash starts with $2a$10$: " + storedHash.startsWith("$2a$10$"));
    }
}