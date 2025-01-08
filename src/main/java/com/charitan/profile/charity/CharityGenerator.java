package com.charitan.profile.charity;

import com.charitan.profile.charity.external.CharityExternalAPI;
import com.charitan.profile.charity.external.dtos.CharityCreationRequest;
import com.charitan.profile.charity.internal.CharityRepository;
import com.charitan.profile.donor.external.DonorExternalAPI;
import com.charitan.profile.donor.external.dtos.DonorCreationRequest;
import com.charitan.profile.donor.internal.DonorRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CharityGenerator {

    private final CharityRepository charityRepository;

    private final CharityExternalAPI charityExternalAPI;

    @PostConstruct
    public void generateDonors() {
        if (charityRepository.findAll().isEmpty()) {
            List<CharityCreationRequest> charities = List.of(
                    // Individual from Vietnam
                    new CharityCreationRequest(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                            "charity0@gmail.com",
                            "Individual Vietnam",
                            "Vietnam",
                            "VietnamAddress",
                            "INDIVIDUAL",
                            "/123e4567-e89b-12d3-a456-426614174000"),
                    // Individual from USA
                    new CharityCreationRequest(UUID.fromString("9b75c19d-f70d-4c6d-b10b-3d517bbac1c8"),
                            "charity1@gmail.com",
                            "Individual USA",
                            "USA",
                            "USAAddress",
                            "INDIVIDUAL",
                            "/9b75c19d-f70d-4c6d-b10b-3d517bbac1c8"),
                    // Company from South Africa
                    new CharityCreationRequest(UUID.fromString("6f9619ff-8b86-d011-b42d-00cf4fc964ff"),
                            "charity2@gmail.com",
                            "Company South Africa",
                            "South Africa",
                            "SAAddress",
                            "COMPANY",
                            "/6f9619ff-8b86-d011-b42d-00cf4fc964ff"),
                    // Company from Germany
                    new CharityCreationRequest(UUID.fromString("d4e7aef8-629f-4419-91a7-8f3eb5bb5e2b"),
                            "charity3@gmail.com",
                            "Company Germany",
                            "Germany",
                            "GermanyAddress",
                            "COMPANY",
                            "/d4e7aef8-629f-4419-91a7-8f3eb5bb5e2b"),
                    // Non-profit organization from Ukraine
                    new CharityCreationRequest(UUID.fromString("72d4e2c7-85a7-4d90-8135-ef7418c39b1d"),
                            "charity4@gmail.com",
                            "Non-Profit Ukraine",
                            "Ukraine",
                            "UkraineAddress",
                            "NON_PROFIT",
                            "/72d4e2c7-85a7-4d90-8135-ef7418c39b1d"),
                    // Non-profit organization from Israel
                    new CharityCreationRequest(UUID.fromString("14eeb072-6635-45c3-aad5-7e76fda0b26e"),
                            "charity5@gmail.com",
                            "Non-Profit Israel",
                            "Israel",
                            "IsraelAddress",
                            "NON_PROFIT",
                            "/14eeb072-6635-45c3-aad5-7e76fda0b26e")
            );

             for (CharityCreationRequest charity : charities) {
                System.out.println("Save charities");
                charityExternalAPI.createCharity(charity);
            }
        }

    }
}

