package com.charitan.profile.charity.repository;

import com.charitan.profile.charity.entity.Charity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharityRepository extends JpaRepository<Charity, Long> {
}
