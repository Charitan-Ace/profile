package com.charitan.profile.user.dto;

import com.charitan.profile.user.entity.User;
import lombok.Getter;

@Getter
public class UserAuthDTO {

    private final Long id;
    private final String email;
    private final boolean isVerfied;
    private final String role;

    public UserAuthDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.isVerfied = user.isVerified();
        this.role = user.getRole().getName();
    }

}
