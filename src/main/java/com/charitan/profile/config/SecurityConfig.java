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
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class SecurityConfig {
    private final String[] PUBLIC_ENDPOINT = { "/donor/getAll",
           "/api-docs", "/v3/api-docs", "/api-docs.yaml", "/swagger-ui/**"};

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request -> request
//                        .requestMatchers(PUBLIC_ENDPOINT).permitAll()
//                        .anyRequest().authenticated())
                        .anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }
}
