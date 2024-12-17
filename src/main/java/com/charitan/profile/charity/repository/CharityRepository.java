package com.charitan.profile.charity.repository;

import com.charitan.profile.charity.entity.Charity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CharityRepository extends JpaRepository<Charity, UUID> {
}
