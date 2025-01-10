package com.charitan.profile.charity.external;

import com.charitan.profile.charity.external.dtos.CharityCreationRequest;
import com.charitan.profile.charity.external.dtos.ExternalCharityDTO;
import com.charitan.profile.charity.internal.dtos.CharityDTO;
import com.charitan.profile.charity.internal.dtos.CharityUpdateRequest;

import java.util.UUID;

public interface CharityExternalAPI {
    public void createCharity(CharityCreationRequest request);
    public ExternalCharityDTO getCharity(UUID userId);
}
