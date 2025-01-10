package com.charitan.profile.donor.external;

import com.charitan.profile.donor.external.dtos.DonorCreationRequest;
import com.charitan.profile.donor.external.dtos.DonorTransactionDTO;
import com.charitan.profile.donor.external.dtos.ExternalDonorDTO;

import java.util.UUID;

public interface DonorExternalAPI {
    public void createDonor(DonorCreationRequest request);
    public DonorTransactionDTO getInfoForTransaction(UUID userId);
    public ExternalDonorDTO getDonor(UUID userId);
}
