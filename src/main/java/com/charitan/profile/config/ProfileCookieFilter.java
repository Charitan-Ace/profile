package com.charitan.profile.config;

import com.charitan.profile.jwt.external.JwtExternalAPI;
import com.charitan.profile.jwt.internal.CustomUserDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ProfileCookieFilter extends OncePerRequestFilter {
  private final String cookieName;
  private final JwtExternalAPI jwtExternalAPI;

  ProfileCookieFilter(
      @Value("${auth.cookie.name:charitan}") String cookieName, JwtExternalAPI jwtExternalAPI) {
    this.cookieName = cookieName;
    this.jwtExternalAPI = jwtExternalAPI;
  }

  @Override
  protected void doFilterInternal(
      @NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain)
      throws ServletException, IOException {
    if (SecurityContextHolder.getContext().getAuthentication() != null
        && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
      filterChain.doFilter(request, response);
      return;
    }
    String path = request.getRequestURI();
    logger.info(path);

    Cookie[] cookies = request.getCookies();
    System.out.println("Cookies: " + Arrays.toString(cookies));
    if (cookies == null) {
      filterChain.doFilter(request, response);
      return;
    }

    Cookie authCookie = null;
    for (Cookie cookie : cookies) {
      if (cookieName.equals(cookie.getName())) {
        authCookie = cookie;
        break;
      }
    }

    if (authCookie == null) {
      filterChain.doFilter(request, response);
      return;
    }

    Claims claims = jwtExternalAPI.parseJwsPayload(authCookie.getValue());
    String id = claims.get("id", String.class);
    String roleId = claims.get("roleId", String.class);
    String email = claims.get("email", String.class);

    var details =
        new CustomUserDetails(
            UUID.fromString(id), email, List.of(new SimpleGrantedAuthority(roleId)));

    UsernamePasswordAuthenticationToken token =
        new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
    token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

    SecurityContextHolder.getContext().setAuthentication(token);

    logger.info("Authenticated as " + details.getUsername());

    filterChain.doFilter(request, response);
  }
}
