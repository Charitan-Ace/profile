package com.charitan.profile.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class SecurityConfig {
    private final String[] PUBLIC_ENDPOINT = { "/donor/getAll",
           "/api-docs", "/v3/api-docs", "/api-docs.yaml", "/swagger-ui/**"};

    private final ProfileCookieFilter profileCookieFilter;

    SecurityConfig(ProfileCookieFilter profileCookieFilter) {
        this.profileCookieFilter = profileCookieFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> requests
                        .anyRequest().permitAll()
                )
                .addFilterBefore(profileCookieFilter, UsernamePasswordAuthenticationFilter.class); // Add custom filter

        return httpSecurity.build();
    }
}
