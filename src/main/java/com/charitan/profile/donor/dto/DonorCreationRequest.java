package com.charitan.profile.donor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class DonorCreationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "First name is required")
    private String firstName;

    @NotNull(message = "Last name is required")
    private String lastName;

    private String address;
}
