package com.charitan.profile.donor;

import com.charitan.profile.donor.dto.DonorCreationRequest;
import com.charitan.profile.donor.dto.DonorDTO;
import com.charitan.profile.donor.dto.DonorUpdateRequest;

public interface DonorExternalAPI {
    public void createDonor(DonorCreationRequest request);
    public void updateDonor(DonorUpdateRequest request);
    public DonorDTO getInfo(Long userId);
}
