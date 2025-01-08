package com.charitan.profile.charity.internal;

import com.charitan.profile.donor.internal.Donor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CharityRepository extends JpaRepository<Charity, UUID> {
    boolean existsCharityByTaxCode(String taxCode);
    Page<Charity> findByCompanyNameContainingIgnoreCase(String companyName, Pageable pageable);
    Page<Charity> findByTaxCodeContainingIgnoreCase(String taxCode, Pageable pageable);
    Page<Charity> findByOrganizationType(OrganizationType organizationType, Pageable pageable);

}
