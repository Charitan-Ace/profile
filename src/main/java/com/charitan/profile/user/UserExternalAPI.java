package com.charitan.profile.user;

import com.charitan.profile.user.dto.UserDTO;

import java.util.Optional;

public interface UserExternalAPI {
    public Optional<UserDTO> findUserById(Long userId);
    public Optional<UserDTO> findUserByEmail(String email);
}
