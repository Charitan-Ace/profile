package com.charitan.profile.charity.internal;

import com.charitan.profile.charity.internal.dtos.CharityDTO;
import com.charitan.profile.charity.internal.dtos.CharitySelfUpdateRequest;
import com.charitan.profile.charity.internal.dtos.CharityUpdateRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface CharityInternalAPI {
  public CharityDTO updateCharity(CharityUpdateRequest request);

  public CharityDTO getInfo(UUID userId);

  public Page<CharityDTO> getAll(
      int pageNo, int pageSize, String order, String filter, String keyword);

  public CharityDTO getMyInfo();

  public CharityDTO updateMyInfo(CharitySelfUpdateRequest request, UUID userId);
}
