package com.charitan.profile.kafka.listener;

import com.charitan.profile.charity.CharityExternalAPI;
import com.charitan.profile.charity.dto.CharityCreationRequest;
import com.charitan.profile.donor.DonorExternalAPI;
import com.charitan.profile.donor.dto.DonorCreationRequest;
import com.charitan.profile.kafka.dto.AuthDetailsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthEventListener {

    private final DonorExternalAPI donorExternalAPI;
    private final CharityExternalAPI charityExternalAPI;

    @KafkaListener(topics = "auth.donor.created", groupId = "auth-group", containerFactory = "multiTypeKafkaListenerContainerFactory")
    public void handleDonorCreatedEvent(AuthDetailsDTO authDetails) {
        System.out.println("Donor created: ");
        DonorCreationRequest request = new DonorCreationRequest(
                authDetails.getId(),
                authDetails.getEmail(),
                authDetails.getProfile().get("firstName"),
                authDetails.getProfile().get("lastName"),
                authDetails.getProfile().get("address")
        );

        donorExternalAPI.createDonor(request);
    }

    @KafkaListener(topics = "auth.charity.created", groupId = "auth-group",  containerFactory = "multiTypeKafkaListenerContainerFactory")
    public void handleCharityCreatedEvent(AuthDetailsDTO authDetails) {
        CharityCreationRequest request = new CharityCreationRequest(
                authDetails.getId(),
                authDetails.getEmail(),
                authDetails.getProfile().get("companyName"),
                authDetails.getProfile().get("address"),
                authDetails.getProfile().get("taxCode"),
                authDetails.getProfile().get("organizationType")
        );

        charityExternalAPI.createCharity(request);
    }
}
