package com.charitan.profile.charity.service;

import com.charitan.profile.charity.dto.CharityCreationRequest;
import com.charitan.profile.charity.dto.CharityDTO;
import com.charitan.profile.charity.dto.CharityUpdateRequest;
import com.charitan.profile.charity.entity.Charity;
import com.charitan.profile.charity.repository.CharityRepository;
import com.charitan.profile.stripe.StripeExternalAPI;
import com.charitan.profile.user.UserExternalAPI;
import com.charitan.profile.user.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class CharityService {
    @Autowired
    private CharityRepository charityRepository;
    private final UserExternalAPI userExternalAPI;
    private final StripeExternalAPI stripeExternalAPI;

    //TODO: send email on successful creation
    public void createCharity(CharityCreationRequest request) {
        UserDTO userDTO = userExternalAPI.findUserById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not found."));

        if (charityRepository.existsById(userDTO.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Charity is already created.");
        }

        if (!userDTO.isVerfied()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not verified.");
        }

        if (!userDTO.getRole().getName().equals("CHARITY")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not charity.");
        }

        String stripeId;
        try {
            stripeId = stripeExternalAPI.createStripeCustomer(
                    userDTO.getEmail(),
                    request.getCompanyName(),
                    "Charity ID: " + userDTO.getId(),
                    Map.of("charityId", String.valueOf(userDTO.getId()))
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user in Stripe: " + e.getMessage());
        }

        Charity charity = new Charity(request.getUserId(), request.getCompanyName(), request.getAddress(), request.getTaxCode(), stripeId);

        charityRepository.save(charity);
    }

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

    public CharityDTO getInfo(Long userId) {

        UserDTO userDTO = userExternalAPI.findUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not found."));

        Charity charity = charityRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Charity not found."));

        return new CharityDTO(charity, userDTO.getEmail());
    }
}
