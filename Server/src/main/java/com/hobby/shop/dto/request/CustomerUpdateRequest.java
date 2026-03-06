package com.hobby.shop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerUpdateRequest {

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;

    @Size(max = 20, message = "Phone must be less than 20 characters")
    private String phone;

    private String address;

    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @Size(max = 20, message = "Postal code must be less than 20 characters")
    private String postalCode;

    @Size(max = 50, message = "Country must be less than 50 characters")
    private String country;
}