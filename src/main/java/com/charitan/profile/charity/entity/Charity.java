package com.charitan.profile.charity.entity;

import com.charitan.profile.charity.enums.OrganizationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="TBL_CHARITIES")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Charity {

    @Id
    @Column(name="user_id")
    private Long userId;

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
}
