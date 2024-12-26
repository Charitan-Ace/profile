package com.charitan.profile.donor.dto;

import com.charitan.profile.donor.entity.Donor;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class DonorDTO implements Serializable {

    private final UUID userId;
    private final String firstName;
    private final String lastName;
    private final String address;
    private final String stripeId;
    private final String assetsKey;

    public DonorDTO(Donor donor) {
        this.userId = donor.getUserId();
        this.firstName = donor.getFirstName();
        this.lastName = donor.getLastName();
        this.address = donor.getAddress();
        this.stripeId = donor.getStripeId();
        this.assetsKey = donor.getAssetsKey();
    }
}
