package com.charitan.profile.charity.dto;

import com.charitan.profile.charity.entity.Charity;
import jakarta.persistence.Column;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CharityDTO {

    private final UUID userId;
    private final String companyName;
    private final String address;
    private final String taxCode;
    private final String stripeId;

    public CharityDTO(Charity charity) {
        this.userId = charity.getUserId();
        this.companyName = charity.getCompanyName();
        this.address = charity.getAddress();
        this.taxCode = charity.getTaxCode();
        this.stripeId = charity.getStripeId();
    }
}
