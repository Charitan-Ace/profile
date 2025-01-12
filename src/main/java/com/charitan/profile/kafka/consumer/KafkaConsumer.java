package com.charitan.profile.kafka.consumer;

import ace.charitan.common.dto.auth.AuthCreationDto;
import ace.charitan.common.dto.profile.charity.*;
import ace.charitan.common.dto.profile.donor.DonorProfileDto;
import ace.charitan.common.dto.profile.donor.DonorsDto;
import ace.charitan.common.dto.profile.donor.GetDonorProfileByIdsRequestDto;
import ace.charitan.common.dto.profile.donor.GetDonorProfileByIdsResponseDto;
import com.charitan.profile.charity.external.CharityExternalAPI;
import com.charitan.profile.charity.external.dtos.CharityCreationRequest;
import com.charitan.profile.charity.external.dtos.ExternalCharityDTO;
import com.charitan.profile.donor.external.DonorExternalAPI;
import com.charitan.profile.donor.external.dtos.DonorCreationRequest;
import com.charitan.profile.donor.external.dtos.ExternalDonorDTO;
import com.charitan.profile.jwt.external.JwtExternalAPI;
import com.charitan.profile.kafka.enums.AuthConsumerTopic;
import com.charitan.profile.kafka.enums.KeyConsumerTopic;
import com.charitan.profile.kafka.enums.ProfileConsumerTopic;
import io.jsonwebtoken.security.Jwks;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AbstractConsumerSeekAware;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;
import java.security.PublicKey;
import java.util.*;

@RequiredArgsConstructor
@Component
public class KafkaConsumer extends AbstractConsumerSeekAware {

    private final DonorExternalAPI donorExternalAPI;
    private final CharityExternalAPI charityExternalAPI;
    private final JwtExternalAPI jwtExternalAPI;

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = AuthConsumerTopic.AUTH_CREATION, groupId = "profile")
    public void handleDonorCreatedEvent(AuthCreationDto authCreationDto) {
        try {
            UUID id = authCreationDto.id(); // Convert String to UUID
            String email = authCreationDto.email();
            String roleId = authCreationDto.roleId();
            Map<String, String> profile = (Map<String, String>) authCreationDto.profile();

            if (roleId.equals("DONOR")) {
                DonorCreationRequest request = new DonorCreationRequest(
                        id,
                        email,
                        profile.get("firstName"),
                        profile.get("lastName"),
                        profile.get("address")
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

    @KafkaListener(
        topics = KeyConsumerTopic.PUBLIC_KEY_CHANGE,
        groupId = "profile-" + "#{T(java.util.UUID).randomUUID()}"
    )
    public void handlePublicKeyChange(String message) {
        try {
            Key jwk = Jwks.parser()
                    .build()
                    .parse(message)
                    .toKey();
            if (jwk instanceof PublicKey) {
                jwtExternalAPI.setSigPublicKey((PublicKey) jwk);
                logger.info("Signature {} public key updated", ((PublicKey) jwk).getFormat());
            }

        } catch (Exception e) {
            logger.error("Failed to process public key created event", e);
            throw new RuntimeException("Failed to process public key created event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = ProfileConsumerTopic.GET_CHARITIES_PROFILE, groupId = "profile")
    @SendTo
    public GetCharityProfileByIdsResponseDto getCharityProfileByIds(GetCharityProfileByIdsRequestDto request) {
        try {
            CharitiesDto resultList = new CharitiesDto(Collections.emptyList());
            for(UUID charityId : request.charityIdList()) {
                ExternalCharityDTO externalCharityDTO = charityExternalAPI.getCharity(charityId);
                if (externalCharityDTO != null) {
                    UUID id = externalCharityDTO.getUserId();
                    String companyName = externalCharityDTO.getCompanyName();
                    String address = externalCharityDTO.getAddress();
                    String taxCode = externalCharityDTO.getTaxCode();
                    OrganizationType organizationType = OrganizationType.valueOf(externalCharityDTO.getOrganizationType().name());
                    String stripeId = externalCharityDTO.getStripeId();
                    String assetKey = externalCharityDTO.getAssetsKey();
                    resultList.charityProfilesList().add(new CharityProfileDto(id, companyName, address, taxCode, organizationType,
                            stripeId, assetKey));
                }
            }
            return new GetCharityProfileByIdsResponseDto(resultList);
        } catch (Exception e) {
            logger.error("Failed to process get stripe id event", e);
            throw new RuntimeException("Failed to process get stripe id event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = ProfileConsumerTopic.GET_DONORS_PROFILE, groupId = "profile")
    @SendTo
    public GetDonorProfileByIdsResponseDto getDonorProfileByIds(GetDonorProfileByIdsRequestDto request) {
        try {
            DonorsDto resultList = new DonorsDto(Collections.emptyList());
            for(UUID donorId : request.donorIdList()) {
                ExternalDonorDTO externalDonorDTO = donorExternalAPI.getDonor(donorId);
                if (externalDonorDTO != null) {
                    UUID id = externalDonorDTO.getUserId();
                    String firstName = externalDonorDTO.getFirstName();
                    String lastName = externalDonorDTO.getLastName();
                    String address = externalDonorDTO.getAddress();
                    String stripeId = externalDonorDTO.getStripeId();
                    String assetKey = externalDonorDTO.getAssetsKey();
                    resultList.donorProfilesList().add(new DonorProfileDto(id, firstName, lastName, address, stripeId, assetKey));
                }
            }
            return new GetDonorProfileByIdsResponseDto(resultList);
        } catch (Exception e) {
            logger.error("Failed to process get stripe id event", e);
            throw new RuntimeException("Failed to process get stripe id event: " + e.getMessage());
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
