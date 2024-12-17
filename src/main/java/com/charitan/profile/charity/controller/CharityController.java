package com.charitan.profile.charity.controller;

import com.charitan.profile.charity.dto.CharityCreationRequest;
import com.charitan.profile.charity.dto.CharityUpdateRequest;
import com.charitan.profile.charity.service.CharityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@Validated
@RequestMapping("api/profile/charity")
public class CharityController {

    @Autowired
    private CharityService charityService;

    @PostMapping("/create")
    public ResponseEntity<String> createCharity(@RequestBody @Valid CharityCreationRequest request) {

        try {
            charityService.createCharity(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Charity registered successfully!");
        } catch (ResponseStatusException e) {
            // If the exception is a ResponseStatusException, return the status and message
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<String> updateDonor(@RequestBody @Valid CharityUpdateRequest request) {

        try {
            charityService.updateCharity(request);
            return ResponseEntity.status(HttpStatus.OK).body("Charity updated successfully!");
        } catch (ResponseStatusException e) {
            // If the exception is a ResponseStatusException, return the status and message
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Object> getInfo(@RequestParam("id") UUID userId) {

        try {
            return ResponseEntity.status(HttpStatus.OK).body(charityService.getInfo(userId));
        } catch (ResponseStatusException e) {
            // If the exception is a ResponseStatusException, return the status and message
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }
}
