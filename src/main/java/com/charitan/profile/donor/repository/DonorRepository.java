package com.charitan.profile.donor.repository;

import com.charitan.profile.donor.entity.Donor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DonorRepository extends JpaRepository<Donor, UUID> {
}
