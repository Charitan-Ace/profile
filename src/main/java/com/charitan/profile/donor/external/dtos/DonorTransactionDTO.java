package com.charitan.profile.donor.external.dtos;

import com.charitan.profile.donor.internal.Donor;
import com.charitan.profile.donor.internal.dtos.DonorDTO;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class DonorTransactionDTO implements Serializable {

  private final String firstName;
  private final String lastName;
  private final String address;
  private final String stripeId;

  public DonorTransactionDTO(DonorDTO donor) {
    this.firstName = donor.getFirstName();
    this.lastName = donor.getLastName();
    this.address = donor.getAddress();
    this.stripeId = donor.getStripeId();
  }

  public DonorTransactionDTO(Donor donor) {
    this.firstName = donor.getFirstName();
    this.lastName = donor.getLastName();
    this.address = donor.getAddress();
    this.stripeId = donor.getStripeId();
  }
}
