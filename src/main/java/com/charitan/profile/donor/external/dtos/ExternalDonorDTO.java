package com.charitan.profile.donor.external.dtos;

import com.charitan.profile.donor.internal.Donor;
import com.charitan.profile.donor.internal.dtos.DonorDTO;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExternalDonorDTO {
  private UUID userId;
  private String firstName;
  private String lastName;
  private String address;
  private String stripeId;
  private String assetsKey;

  public ExternalDonorDTO(Donor donor) {
    this.userId = donor.getUserId();
    this.firstName = donor.getFirstName();
    this.lastName = donor.getLastName();
    this.address = donor.getAddress();
    this.stripeId = donor.getStripeId();
    this.assetsKey = donor.getAssetsKey();
  }

  public ExternalDonorDTO(DonorDTO donor) {
    this.userId = donor.getUserId();
    this.firstName = donor.getFirstName();
    this.lastName = donor.getLastName();
    this.address = donor.getAddress();
    this.stripeId = donor.getStripeId();
    this.assetsKey = donor.getAssetsKey();
  }
}
