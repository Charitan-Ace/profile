package com.charitan.profile.charity;

import com.charitan.profile.charity.dto.CharityCreationRequest;
import com.charitan.profile.charity.dto.CharityDTO;
import com.charitan.profile.charity.dto.CharityUpdateRequest;

import java.util.UUID;

public interface CharityExternalAPI {
    public void createCharity(CharityCreationRequest request);
    public void updateCharity(CharityUpdateRequest request);
    public CharityDTO getInfo(UUID userId);
}
