package com.charitan.profile.donor.internal.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;

@Getter
public class DonorUpdateRequest {
  @NotNull(message = "User ID is required")
  private UUID userId;

  private String firstName;

  private String lastName;

  private String address;
}
