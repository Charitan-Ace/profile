package com.charitan.profile.charity.internal.dtos;

import com.charitan.profile.charity.internal.Charity;
import com.charitan.profile.charity.internal.OrganizationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CharityDTO implements Serializable {
    private UUID userId;
    private String companyName;
    private String address;
    private String taxCode;
    private String stripeId;
    private OrganizationType organizationType;
    @Setter
    private String assetsKey;
    @Setter
    private String video;

    public CharityDTO(Charity charity) {
        this.userId = charity.getUserId();
        this.companyName = charity.getCompanyName();
        this.address = charity.getAddress();
        this.taxCode = charity.getTaxCode();
        this.stripeId = charity.getStripeId();
        this.organizationType = charity.getOrganizationType();
        this.assetsKey = charity.getAssetsKey();
        this.video = charity.getVideo();
    }
}
