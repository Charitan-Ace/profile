package com.charitan.profile.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserCreationRequest {

    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "Confirmed password is required")
    @Size(min = 8, message = "Confirmed password must be at least 8 characters long")
    private String confirmPassword;

    @NotNull(message = "Role is required")
    private String role;
}
