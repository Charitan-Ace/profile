package com.charitan.profile.kafka.consumer;

import com.charitan.profile.charity.external.CharityExternalAPI;
import com.charitan.profile.charity.external.dtos.CharityCreationRequest;
import com.charitan.profile.donor.external.DonorExternalAPI;
import com.charitan.profile.donor.external.dtos.DonorCreationRequest;
import com.charitan.profile.jwt.external.JwtExternalAPI;
import com.charitan.profile.kafka.enums.AuthConsumerTopic;
import com.charitan.profile.kafka.enums.KeyConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWK;
import io.jsonwebtoken.security.Jwks;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AbstractConsumerSeekAware;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.PublicKey;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class KafkaConsumer extends AbstractConsumerSeekAware {

    private final DonorExternalAPI donorExternalAPI;
    private final CharityExternalAPI charityExternalAPI;
    private final JwtExternalAPI jwtExternalAPI;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = AuthConsumerTopic.AUTH_CREATION, groupId = "profile")
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

    @KafkaListener(topics = KeyConsumer.PUBLIC_KEY_CHANGE, groupId = "profile")
    public void handlePublicKeyChange(String message) {
        try {

            // Remove the outer quotes
            if (message.startsWith("\"") && message.endsWith("\"")) {
                message = message.substring(1, message.length() - 1).replace("\\", "");
            }

            System.out.println(message);

            Key jwk = Jwks.parser()
                    .build()
                    .parse(message)
                    .toKey();
            if (jwk instanceof PublicKey) {
                jwtExternalAPI.setSigPublicKey((PublicKey) jwk);
                logger.info("Signature {} public key updated", ((PublicKey) jwk).getFormat());
            }

        } catch (Exception e) {
            logger.error("Failed to process donor created event", e);
            throw new RuntimeException("Failed to process donor created event: " + e.getMessage());
        }
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        for (TopicPartition topicPartition : assignments.keySet()) {
            if ("key.signature.public.change".equals(topicPartition.topic())) {
                callback.seekRelative(topicPartition.topic(), topicPartition.partition(), -1, false);
            }
        }
    }
}
