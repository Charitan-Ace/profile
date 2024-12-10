package com.charitan.profile.config;

import com.charitan.profile.role.Role;
import com.charitan.profile.role.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Configuration
public class AppInitConfig {
    @Bean
    @Transactional
    public CommandLineRunner initDatabase(RoleRepository roleRepository) {
        return args -> {
            try {

                if (roleRepository.count() == 0) {
                    List<String> defaultRoles = Arrays.asList("DONOR", "CHARITY", "ADMIN");

                    for (String roleName : defaultRoles) {
                        Role role = new Role();
                        role.setName(roleName);
                        roleRepository.save(role);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}