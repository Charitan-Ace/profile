package com.charitan.profile.donor.internal.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class DonorUpdateRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    private String firstName;

    private String lastName;

    private String address;
}
