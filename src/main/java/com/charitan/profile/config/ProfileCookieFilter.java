package com.charitan.profile.config;

import com.charitan.profile.jwt.external.JwtExternalAPI;
import com.charitan.profile.jwt.internal.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ProfileCookieFilter extends OncePerRequestFilter {

    private final JwtExternalAPI jwtExternalAPI;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Skip filter if already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Find the authentication cookie
        Cookie authCookie = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("charitan")) {
                    authCookie = cookie;
                    break;
                }
            }
        }

        // If no auth cookie is present, continue the filter chain
        if (authCookie == null) {
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println(authCookie.getValue());

        try {
            // Parse claims from the JWT
            var claims = jwtExternalAPI.parseJwsPayload(authCookie.getValue());

            // Extract details from JWT claims
            String email = claims.get("email", String.class);
            UUID id = UUID.fromString(claims.get("id", String.class));
            String role = claims.get("roleId", String.class);

            List<GrantedAuthority> authorities;
            if (role != null) {
                // Convert the role to a GrantedAuthority
                authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
            } else {
                // Handle case where no role is present (e.g., empty list of authorities)
                authorities = Collections.emptyList();
            }

            // Create UserDetails from JWT claims
            CustomUserDetails userDetails = new CustomUserDetails(id, email, authorities);
            System.out.println(userDetails.toString());

            // Create authentication token and set it in the security context
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    authorities
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);

            logger.info("Authenticated as " + email);

        } catch (Exception e) {
            logger.error("Failed to authenticate using JWT: " + e.getMessage());
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    private String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("charitan")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
