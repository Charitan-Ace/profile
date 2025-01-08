package com.charitan.profile.charity.internal.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CharityUpdateRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private String companyName;

    private String address;

    private String taxCode;

    private String organizationType;
}
