package com.charitan.profile.donor.internal;

import com.charitan.profile.donor.internal.dtos.DonorDTO;
import com.charitan.profile.donor.internal.dtos.DonorSelfUpdateRequest;
import com.charitan.profile.donor.internal.dtos.DonorUpdateRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface DonorInternalAPI {
  public DonorDTO updateDonor(DonorUpdateRequest request);

  public DonorDTO getInfo(UUID userId);

  public Page<DonorDTO> getAll(
      int pageNo, int pageSize, String order, String filter, String keyword);

  public DonorDTO getMyInfo();

  public DonorDTO updateMyInfo(DonorSelfUpdateRequest request, UUID userId);
}
