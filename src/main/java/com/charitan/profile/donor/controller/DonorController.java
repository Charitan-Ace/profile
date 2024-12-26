package com.charitan.profile.donor.controller;

import com.charitan.profile.donor.dto.DonorCreationRequest;
import com.charitan.profile.donor.dto.DonorUpdateRequest;
import com.charitan.profile.donor.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Controller
@RequestMapping("api/profile/donor")
public class DonorController {
    @Autowired
    private DonorService donorService;

    @PostMapping("/create")
    public ResponseEntity<String> createDonor(@RequestBody @Valid DonorCreationRequest request) {

        try {
            donorService.createDonor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Donor registered successfully!");
        } catch (ResponseStatusException e) {
            // If the exception is a ResponseStatusException, return the status and message
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

    @PatchMapping("/update")
    public ResponseEntity<String> updateDonor(@RequestBody @Valid DonorUpdateRequest request) {

        try {
            donorService.updateDonor(request);
            return ResponseEntity.status(HttpStatus.OK).body("Donor updated successfully!");
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
            return ResponseEntity.status(HttpStatus.OK).body(donorService.getInfo(userId));
        } catch (ResponseStatusException e) {
            // If the exception is a ResponseStatusException, return the status and message
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

    @GetMapping("/all")
    ResponseEntity<Object> getAll(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(donorService.getAll(pageNo, pageSize));
        } catch (ResponseStatusException e) {
            // If the exception is a ResponseStatusException, return the status and message
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }
}
