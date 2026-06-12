package com.example.ecommerce.user.dto;

import com.example.ecommerce.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Email @NotBlank @Size(max = 160) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Pattern(regexp = "^$|^[+0-9][0-9\\-\\s()]{7,29}$", message = "must be a valid phone number") String phone,
        @NotNull Role role
) {
}
