package com.charitan.profile.donor.internal.dtos;

import lombok.Getter;

@Getter
public class DonorSelfUpdateRequest {
  private String firstName;

  private String lastName;

  private String address;

  private String avatar;
}
