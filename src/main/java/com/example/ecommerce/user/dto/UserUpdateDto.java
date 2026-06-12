package com.example.ecommerce.user.dto;

import com.example.ecommerce.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateDto(
        @Size(max = 80) String firstName,
        @Size(max = 80) String lastName,
        @Email @Size(max = 160) String email,
        @Pattern(regexp = "^$|^[+0-9][0-9\\-\\s()]{7,29}$", message = "must be a valid phone number") String phone,
        Role role
) {
}
