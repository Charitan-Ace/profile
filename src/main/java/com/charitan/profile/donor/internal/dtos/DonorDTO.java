package com.charitan.profile.donor.internal.dtos;

import com.charitan.profile.donor.internal.Donor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class DonorDTO implements Serializable {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String address;
    private String stripeId;
    @Setter
    private String assetsKey;

    public DonorDTO(Donor donor) {
        this.userId = donor.getUserId();
        this.firstName = donor.getFirstName();
        this.lastName = donor.getLastName();
        this.address = donor.getAddress();
        this.stripeId = donor.getStripeId();
        this.assetsKey = donor.getAssetsKey();
    }
}
