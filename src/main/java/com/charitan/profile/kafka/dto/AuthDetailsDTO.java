package com.charitan.profile.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Setter
public class AuthDetailsDTO {
    private UUID id;
    private String email;
    private String roleId;
    private Map<String, String> profile;

    @JsonProperty("id")
    public UUID getId() {
        return id;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("roleId")
    public String getRoleId() {
        return roleId;
    }

    @JsonProperty("profile")
    public Map<String, String> getProfile() {
        return profile;
    }

}

