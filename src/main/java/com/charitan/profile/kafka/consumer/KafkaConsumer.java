package com.charitan.profile.kafka.consumer;

import com.charitan.profile.charity.CharityExternalAPI;
import com.charitan.profile.charity.dto.CharityCreationRequest;
import com.charitan.profile.donor.DonorExternalAPI;
import com.charitan.profile.donor.dto.DonorCreationRequest;
import com.charitan.profile.kafka.enums.AuthConsumerTopic;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class KafkaConsumer {

    private final DonorExternalAPI donorExternalAPI;
    private final CharityExternalAPI charityExternalAPI;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = AuthConsumerTopic.AUTH_CREATION, groupId = "auth-group")
    public void handleDonorCreatedEvent(String message) {
        try {
            // Parse JSON string to a Map
            Map<String, Object> authDetails = objectMapper.readValue(message, Map.class);

            UUID id = UUID.fromString(authDetails.get("id").toString()); // Convert String to UUID
            String email = authDetails.get("email").toString();
            String roleId = authDetails.get("roleId").toString();
            Map<String, String> profile = (Map<String, String>) authDetails.get("profile");

            if (roleId.equals("DONOR")) {
                DonorCreationRequest request = new DonorCreationRequest(
                        id,
                        email,
                        profile.get("firstName"),
                        profile.get("lastName"),
                        profile.get("address"),
                        profile.get("assetsKey")
                );

                donorExternalAPI.createDonor(request);
            } else if (roleId.equals("CHARITY")) {
                CharityCreationRequest request = new CharityCreationRequest(
                        id,
                        email,
                        profile.get("companyName"),
                        profile.get("address"),
                        profile.get("taxCode"),
                        profile.get("organizationType"),
                        profile.get("assetsKey")
                );

                charityExternalAPI.createCharity(request);
            }
        } catch (Exception e) {
            System.err.println("Failed to process donor created event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
