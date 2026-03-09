package com.hobby.shop.security.crypto.bcrypt;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswords {
    public static void main(String[] args) {
        // Run this once
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("User hash: " + encoder.encode("password123"));
        System.out.println("Admin hash: " + encoder.encode("password123"));
    }
}