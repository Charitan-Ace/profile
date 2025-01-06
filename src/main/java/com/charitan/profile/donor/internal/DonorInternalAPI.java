package com.charitan.profile.donor.internal;

import com.charitan.profile.donor.internal.dtos.DonorDTO;
import com.charitan.profile.donor.internal.dtos.DonorUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface DonorInternalAPI {
    public void updateDonor(DonorUpdateRequest request);
    public DonorDTO getInfo(UUID userId);
    public Page<DonorDTO> getAll(int pageNo, int pageSize, String order, String filter, String keyword);
}
