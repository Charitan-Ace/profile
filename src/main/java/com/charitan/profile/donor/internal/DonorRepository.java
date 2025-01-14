package com.charitan.profile.donor.internal;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonorRepository extends JpaRepository<Donor, UUID> {
  Page<Donor> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

  Page<Donor> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);
}
