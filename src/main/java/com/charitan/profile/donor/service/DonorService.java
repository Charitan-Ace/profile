package com.charitan.profile.donor.service;

import com.charitan.profile.donor.dto.DonorCreationRequest;
import com.charitan.profile.donor.dto.DonorDTO;
import com.charitan.profile.donor.dto.DonorUpdateRequest;
import com.charitan.profile.donor.entity.Donor;
import com.charitan.profile.donor.repository.DonorRepository;
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
public class DonorService {
    @Autowired
    private DonorRepository donorRepository;
    private final StripeExternalAPI stripeExternalAPI;
    private final UserExternalAPI userExternalAPI;

    //TODO: send email on successful creation
    public void createDonor(DonorCreationRequest request) {
        UserDTO userDTO = userExternalAPI.findUserById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not found."));

        if (donorRepository.existsById(userDTO.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Donor is already created.");
        }

        if (!userDTO.isVerfied()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not verified.");
        }

        if (!userDTO.getRole().getName().equals("DONOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not donor.");
        }

        String stripeId;
        try {
            stripeId = stripeExternalAPI.createStripeCustomer(
                    userDTO.getEmail(),
                    request.getFirstName() + " " + request.getLastName(),
                    "Donor ID: " + userDTO.getId(),
                    Map.of("donorId", String.valueOf(userDTO.getId()))
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user in Stripe: " + e.getMessage());
        }

        Donor donor = new Donor(userDTO.getId(), request.getLastName(), request.getFirstName(), request.getAddress(), stripeId);

        donorRepository.save(donor);
    }

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

    public DonorDTO getInfo(Long userId) {

        UserDTO userDTO = userExternalAPI.findUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not found."));

        Donor donor = donorRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor not found."));

        return new DonorDTO(donor, userDTO.getEmail());
    }
}
