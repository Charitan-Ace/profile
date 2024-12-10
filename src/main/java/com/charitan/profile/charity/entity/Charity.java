package com.charitan.profile.charity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(name="stripe_id")
    private String stripeId;
}
