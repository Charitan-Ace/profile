package com.charitan.profile.charity.internal.dtos;

import lombok.Getter;

@Getter
public class CharitySelfUpdateRequest {
    private String companyName;

    private String address;

    private String taxCode;

    private String organizationType;
}
