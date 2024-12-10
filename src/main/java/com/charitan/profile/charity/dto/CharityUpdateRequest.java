package com.charitan.profile.charity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CharityUpdateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private String companyName;

    private String address;

    private String taxCode;
}
