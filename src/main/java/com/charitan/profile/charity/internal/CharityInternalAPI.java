package com.charitan.profile.charity.internal;

import com.charitan.profile.charity.internal.dtos.CharityDTO;
import com.charitan.profile.charity.internal.dtos.CharityUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface CharityInternalAPI {
    public void updateCharity(CharityUpdateRequest request);
    public CharityDTO getInfo(UUID userId);
    public Page<CharityDTO> getAll(int pageNo, int pageSize, String order, String filter, String keyword);
}
