package com.charitan.profile.charity.external.dtos;

import com.charitan.profile.charity.internal.Charity;
import com.charitan.profile.charity.internal.OrganizationType;
import com.charitan.profile.charity.internal.dtos.CharityDTO;
import java.io.Serializable;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExternalCharityDTO implements Serializable {

  private UUID userId;
  private String companyName;
  private String address;
  private String taxCode;
  private String stripeId;
  private OrganizationType organizationType;
  private String assetsKey;

  public ExternalCharityDTO(CharityDTO charity) {
    this.userId = charity.getUserId();
    this.companyName = charity.getCompanyName();
    this.address = charity.getAddress();
    this.taxCode = charity.getTaxCode();
    this.stripeId = charity.getStripeId();
    this.organizationType = charity.getOrganizationType();
    this.assetsKey = charity.getAssetsKey();
  }

  public ExternalCharityDTO(Charity charity) {
    this.userId = charity.getUserId();
    this.companyName = charity.getCompanyName();
    this.address = charity.getAddress();
    this.taxCode = charity.getTaxCode();
    this.stripeId = charity.getStripeId();
    this.organizationType = charity.getOrganizationType();
    this.assetsKey = charity.getAssetsKey();
  }
}
