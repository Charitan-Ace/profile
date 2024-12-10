package com.charitan.profile.user.dto;

import com.charitan.profile.role.Role;
import com.charitan.profile.user.entity.User;
import lombok.Getter;

@Getter
public class UserDTO {

    private final Long id;
    private final String email;
    private final String password;
    private final boolean isVerfied;
    private final Role role;

    public UserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.isVerfied = user.isVerified();
        this.role = user.getRole();
    }
}
