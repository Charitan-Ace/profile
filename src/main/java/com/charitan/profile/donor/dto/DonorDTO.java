package com.charitan.profile.donor.dto;

import com.charitan.profile.donor.entity.Donor;
import lombok.Getter;

@Getter
public class DonorDTO {

    private final Long userId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String address;
    private final String stripeId;

    public DonorDTO(Donor donor, String email) {
        this.userId = donor.getUserId();
        this.email = email;
        this.firstName = donor.getFirstName();
        this.lastName = donor.getLastName();
        this.address = donor.getAddress();
        this.stripeId = donor.getStripeId();
    }
}
