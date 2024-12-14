package com.charitan.profile.charity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CharityCreationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Company name is required")
    private String companyName;

    @NotNull(message = "Address is required")
    private String address;

    @NotNull(message = "Tax code is required")
    private String taxCode;

    @NotNull(message = "Organization type is required")
    private String organizationType;
}
