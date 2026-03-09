package com.hobby.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String token;
    private List<String> roles;
    private LocalDateTime createdAt;
}