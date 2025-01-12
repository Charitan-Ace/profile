package com.charitan.profile.charity.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="TBL_CHARITIES")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Charity {
    @Id
    @Column(name="user_id")
    private UUID userId;

    @Column(name="company_name")
    private String companyName;

    @Column(name="address")
    private String address;

    @Column(name="tax_code")
    private String taxCode;

    @Column(name="organization_type")
    @Enumerated(EnumType.STRING) // Use STRING to store enum names
    private OrganizationType organizationType;

    @Column(name="stripe_id")
    private String stripeId;

    @Column(name="assets_key")
    private String assetsKey;

    @Column
    private String video;
}
