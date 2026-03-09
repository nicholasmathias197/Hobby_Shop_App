package com.hobby.shop.security.crypto.bcrypt;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // The password you're trying to login with
        String rawPassword = "password123";

        // PASTE THE ACTUAL HASH FROM YOUR DATABASE HERE
        String storedHash = "$2a$10$XURPShQNCsLjp1ESc2laoObo9QZDhxz73hJPaEv7/cBha4pk0AgP.";

        boolean matches = encoder.matches(rawPassword, storedHash);
        System.out.println("Password matches: " + matches);

        // Also show what a fresh hash looks like for comparison
        String freshHash = encoder.encode(rawPassword);
        System.out.println("Fresh hash of 'password123': " + freshHash);
    }
}