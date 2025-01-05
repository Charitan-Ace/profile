package com.charitan.profile.donor;

import com.charitan.profile.donor.external.DonorExternalAPI;
import com.charitan.profile.donor.external.dtos.DonorCreationRequest;
import com.charitan.profile.donor.internal.DonorInternalAPI;
import com.charitan.profile.donor.internal.DonorRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DonorGenerator {

    private final DonorRepository donorRepository;

    private final DonorExternalAPI donorExternalAPI;

    @PostConstruct
    public void generateDonors() {
        if (donorRepository.findAll().isEmpty()) {
            List<DonorCreationRequest> donors = List.of(
                    new DonorCreationRequest(UUID.fromString("7e5bcf25-bafd-4dcc-a6ed-46be277ece3f"),
                            "donor0@gmail.com",
                            "Trung",
                            "Le",
                            "donor0Address"
                            , "/7e5bcf25-bafd-4dcc-a6ed-46be277ece3f"),
                    new DonorCreationRequest(UUID.fromString("40ccc062-ca27-436b-85de-7f0b0b795b5f"),
                            "donor1@gmail.com",
                            "Nguyen",
                            "Do",
                            "donor1Address"
                            , "/40ccc062-ca27-436b-85de-7f0b0b795b5f"),
                    new DonorCreationRequest(UUID.fromString("bf8a567f-75fd-49ba-b684-d748eb9bbe49"),
                            "donor2@gmail.com",
                            "Pavel",
                            "Potemkin",
                            "donor2Address"
                            , "/bf8a567f-75fd-49ba-b684-d748eb9bbe49"),
                    new DonorCreationRequest(UUID.fromString("257949d1-dcb3-4443-9992-8d3449149a49"),
                            "donor3@gmail.com",
                            "Saurabh",
                            "Padmakumar",
                            "donor3Address"
                            , "/257949d1-dcb3-4443-9992-8d3449149a49"),
                    new DonorCreationRequest(UUID.fromString("9e478b92-4d5f-4e43-a400-7a46d6483aed"),
                            "donor4@gmail.com",
                            "Kien",
                            "Nguyen",
                            "donor4Address"
                            , "/9e478b92-4d5f-4e43-a400-7a46d6483aed")
            );

            for (DonorCreationRequest donor : donors) {
                System.out.println("Save donors");
                donorExternalAPI.createDonor(donor);
            }
            // applicantRepo.saveAll(customers);
        }

    }
}
