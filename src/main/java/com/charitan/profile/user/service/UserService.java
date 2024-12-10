package com.charitan.profile.user.service;

import com.charitan.profile.role.Role;
import com.charitan.profile.role.RoleRepository;
import com.charitan.profile.user.UserExternalAPI;
import com.charitan.profile.user.dto.UserCreationRequest;
import com.charitan.profile.user.dto.UserDTO;
import com.charitan.profile.user.entity.User;
import com.charitan.profile.user.repository.UserRepository;
import org.antlr.v4.runtime.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Transactional
@Service
public class UserService implements UserExternalAPI {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    public String createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
        }

        if (!request.getConfirmPassword().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords are not matched.");
        }

        Optional<Role> role = roleRepository.findByName(request.getRole());
        if (role.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role.");
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        User user = new User();
        user.setEmail(request.getEmail());
        user.setVerified(false);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role.get());
        userRepository.save(user);

        return sendCode(user.getEmail());
//        return new UserAuthDTO(user);
    }

    //TODO: Send the code to user's email instead of return as response body
    public String sendCode(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Get user ID and calculate time 30 minutes from now
        Long userId = user.getId();
        LocalDateTime timePlus30Minutes = LocalDateTime.now().plusMinutes(30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Return the formatted string
        return formatForUrl(String.format("User ID: %d, Expiry Time: %s", userId, timePlus30Minutes.format(formatter)));
    }

    public String verifyUser(String token) {

        System.out.println(token);

        // Split the string by delimiters
        String[] parts = token.split(", ");
        String idPart = parts[0];
        String timePart = parts[1];

        // Extract the numeric ID
        Long userId = Long.parseLong(idPart.split(": ")[1]);

        // Extract and parse the time
        String timeString = timePart.split(": ")[1];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime expiryTime = LocalDateTime.parse(timeString, formatter);
        LocalDateTime now = LocalDateTime.now();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        if (expiryTime.isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "The code has expired.");
        }

        user.setVerified(true);
        userRepository.save(user);

        return "User verify successfully.";
    }

    // TODO: encrypt the string with key service
    private String formatForUrl(String token) {
        try {
            String encodedString = URLEncoder.encode(token, StandardCharsets.UTF_8);
            return "http://localhost:8080/charitan/api/user/verify?token=" + encodedString;
        } catch (Exception e) {
            throw new RuntimeException("Error encoding URL", e);
        }
    }

    @Override
    public Optional<UserDTO> findUserById(Long userId) {
        return userRepository.findById(userId)
                .map(UserDTO::new);
    }

    @Override
    public Optional<UserDTO> findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserDTO::new);
    }

}

