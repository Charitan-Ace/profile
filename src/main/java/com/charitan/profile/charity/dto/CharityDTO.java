package com.charitan.profile.charity.dto;

import com.charitan.profile.charity.entity.Charity;
import jakarta.persistence.Column;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class CharityDTO implements Serializable {

    private final UUID userId;
    private final String companyName;
    private final String address;
    private final String taxCode;
    private final String stripeId;
    private final String assetsKey;

    public CharityDTO(Charity charity) {
        this.userId = charity.getUserId();
        this.companyName = charity.getCompanyName();
        this.address = charity.getAddress();
        this.taxCode = charity.getTaxCode();
        this.stripeId = charity.getStripeId();
        this.assetsKey = charity.getAssetsKey();
    }
}
