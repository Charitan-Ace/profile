package com.charitan.profile.charity.external.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CharityCreationRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "User email is required")
    private String email;

    @NotNull(message = "Company name is required")
    private String companyName;

    @NotNull(message = "Address is required")
    private String address;

    @NotNull(message = "Tax code is required")
    private String taxCode;

    @NotNull(message = "Organization type is required")
    private String organizationType;
}
