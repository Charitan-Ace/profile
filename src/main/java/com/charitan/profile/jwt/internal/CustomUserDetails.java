package com.charitan.profile.jwt.internal;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {
  private UUID userId;
  private String email;
  private List<GrantedAuthority> authorities;

  // Constructor, getters, and setters

  public CustomUserDetails(UUID userId, String email, List<GrantedAuthority> authorities) {
    this.userId = userId;
    this.email = email;
    this.authorities = authorities;
  }

  @Override
  public String getUsername() {
    return email;
  }

  public UUID getUserId() {
    return userId;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return null; // Password should not be required for JWT-based authentication
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
