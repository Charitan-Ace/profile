package com.charitan.profile.donor.service;

import com.charitan.profile.donor.DonorExternalAPI;
import com.charitan.profile.donor.dto.DonorCreationRequest;
import com.charitan.profile.donor.dto.DonorDTO;
import com.charitan.profile.donor.dto.DonorUpdateRequest;
import com.charitan.profile.donor.entity.Donor;
import com.charitan.profile.donor.repository.DonorRepository;
import com.charitan.profile.stripe.StripeExternalAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DonorService implements DonorExternalAPI {
    @Autowired
    private DonorRepository donorRepository;
    private final StripeExternalAPI stripeExternalAPI;

    //TODO: send email on successful creation
    @Override
    public void createDonor(DonorCreationRequest request) {

        if (donorRepository.existsById(request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Donor is already created.");
        }

        String stripeId;
        try {
            stripeId = stripeExternalAPI.createStripeCustomer(
                    request.getEmail(),
                    request.getFirstName() + " " + request.getLastName(),
                    "Donor ID: " + request.getUserId(),
                    Map.of("donorId", String.valueOf(request.getUserId()))
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user in Stripe: " + e.getMessage());
        }

        Donor donor = new Donor(request.getUserId(), request.getLastName(), request.getFirstName(), request.getAddress(), stripeId);

        donorRepository.save(donor);
    }

    @Override
    public void updateDonor(DonorUpdateRequest request) {

        Donor donor = donorRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found."));

        if (request.getFirstName() != null) {
            donor.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            donor.setLastName(request.getLastName());
        }
        if (request.getAddress() != null) {
            donor.setAddress(request.getAddress());
        }

        donorRepository.save(donor);
    }

    //TODO: get email from auth service
    @Override
    public DonorDTO getInfo(UUID userId) {

        Donor donor = donorRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found."));

        return new DonorDTO(donor, "dummy@gmail.com");
    }
}
