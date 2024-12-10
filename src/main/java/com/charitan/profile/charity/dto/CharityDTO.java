package com.charitan.profile.charity.dto;

import com.charitan.profile.charity.entity.Charity;
import jakarta.persistence.Column;
import lombok.Getter;

@Getter
public class CharityDTO {

    private final Long userId;
    private final String email;
    private final String companyName;
    private final String address;
    private final String taxCode;
    private final String stripeId;

    public CharityDTO(Charity charity, String email) {
        this.userId = charity.getUserId();
        this.email = email;
        this.companyName = charity.getCompanyName();
        this.address = charity.getAddress();
        this.taxCode = charity.getTaxCode();
        this.stripeId = charity.getStripeId();
    }
}
