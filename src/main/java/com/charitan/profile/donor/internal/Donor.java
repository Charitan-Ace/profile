package com.charitan.profile.donor.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TBL_DONORS")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Donor {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "address")
  private String address;

  @Column(name = "stripe_id")
  private String stripeId;

  @Column(name = "assets_key")
  private String assetsKey;
}
