package com.charitan.profile.donor.external.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DonorCreationRequest {

  @NotNull(message = "User ID is required")
  private UUID userId;

  @NotNull(message = "Email is required")
  private String email;

  @NotNull(message = "First name is required")
  private String firstName;

  @NotNull(message = "Last name is required")
  private String lastName;

  private String address;
}
