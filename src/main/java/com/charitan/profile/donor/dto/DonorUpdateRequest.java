package com.charitan.profile.donor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class DonorUpdateRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    private String firstName;

    private String lastName;

    private String address;
}
