package com.charitan.profile.donor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class DonorCreationRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Email is required")
    private String email;

    @NotNull(message = "First name is required")
    private String firstName;

    @NotNull(message = "Last name is required")
    private String lastName;

    private String address;
}
