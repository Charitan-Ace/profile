package com.charitan.profile.user.controller;

import com.charitan.profile.exception.ErrorResponse;
import com.charitan.profile.user.dto.UserCreationRequest;
import com.charitan.profile.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Validated
@RequestMapping("/api/profile/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Object> createUser(@RequestBody @Valid UserCreationRequest request) {
        try {
//            UserAuthDTO response = userService.createUser(request);
            String response = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ResponseStatusException e) {
            // If the exception is a ResponseStatusException, return the status and message
            ErrorResponse errorResponse = new ErrorResponse(e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
        } catch (Exception e) {
            // Handle other exceptions
            ErrorResponse errorResponse = new ErrorResponse("An error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/sendCode")
    public ResponseEntity<Object> sendCode(@RequestParam("userEmail") String userEmail) {
        try {
            String response = userService.sendCode(userEmail);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ResponseStatusException e) {
            // If the exception is a ResponseStatusException, return the status and message
            ErrorResponse errorResponse = new ErrorResponse(e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
        } catch (Exception e) {
            // Handle other exceptions
            ErrorResponse errorResponse = new ErrorResponse("An error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<Object> verifyUser(@RequestParam("token") String token) {
        try {
            String response = userService.verifyUser(token);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ResponseStatusException e) {
            // If the exception is a ResponseStatusException, return the status and message
            ErrorResponse errorResponse = new ErrorResponse(e.getReason());
            return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
        } catch (Exception e) {
            // Handle other exceptions
            ErrorResponse errorResponse = new ErrorResponse("An error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
