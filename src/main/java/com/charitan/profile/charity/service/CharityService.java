package com.charitan.profile.charity.service;

import com.charitan.profile.charity.CharityExternalAPI;
import com.charitan.profile.charity.dto.CharityCreationRequest;
import com.charitan.profile.charity.dto.CharityDTO;
import com.charitan.profile.charity.dto.CharityUpdateRequest;
import com.charitan.profile.charity.entity.Charity;
import com.charitan.profile.charity.enums.OrganizationType;
import com.charitan.profile.charity.repository.CharityRepository;
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
public class CharityService implements CharityExternalAPI {
    @Autowired
    private CharityRepository charityRepository;
    private final StripeExternalAPI stripeExternalAPI;

    //TODO: send email on successful creation
    @Override
    public void createCharity(CharityCreationRequest request) {

        if (charityRepository.existsById(request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Charity is already created.");
        }

        // Validate and parse organizationType
        OrganizationType organizationType;
        try {
            organizationType = OrganizationType.valueOf(request.getOrganizationType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid organization type.");
        }

        String stripeId;
        try {
            stripeId = stripeExternalAPI.createStripeCustomer(
                    request.getEmail(),
                    request.getCompanyName(),
                    "Charity ID: " + request.getUserId(),
                    Map.of("charityId", String.valueOf(request.getUserId()))
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user in Stripe: " + e.getMessage());
        }

        Charity charity = new Charity(request.getUserId(), request.getCompanyName(), request.getAddress(), request.getTaxCode(), organizationType, stripeId);

        charityRepository.save(charity);
    }

    @Override
    public void updateCharity(CharityUpdateRequest request) {

        Charity charity = charityRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Charity not found."));

        if (request.getCompanyName() != null) {
            charity.setCompanyName(request.getCompanyName());
        }
        if (request.getTaxCode() != null) {
            charity.setTaxCode(request.getTaxCode());
        }
        if (request.getAddress() != null) {
            charity.setAddress(request.getAddress());
        }

        charityRepository.save(charity);
    }

    //TODO: get email from auth service
    @Override
    public CharityDTO getInfo(UUID userId) {

        Charity charity = charityRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Charity not found."));

        return new CharityDTO(charity);
    }
}
