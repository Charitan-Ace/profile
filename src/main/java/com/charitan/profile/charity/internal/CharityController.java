package com.charitan.profile.charity.internal;

import com.charitan.profile.charity.internal.dtos.CharityDTO;
import com.charitan.profile.charity.internal.dtos.CharitySelfUpdateRequest;
import com.charitan.profile.charity.internal.dtos.CharityUpdateRequest;
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
    private CharityInternalAPI charityInternalAPI;

    @PatchMapping("/update")
    public ResponseEntity<Object> updateDonor(@RequestBody @Valid CharityUpdateRequest request) {

        try {
            return ResponseEntity.status(HttpStatus.OK).body(charityInternalAPI.updateCharity(request));
        } catch (ResponseStatusException e) {
            // If the exception is a ResponseStatusException, return the status and message
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

    @PatchMapping("/update/me")
    public ResponseEntity<Object> updateMyInfo(@RequestBody @Valid CharitySelfUpdateRequest request) {

        try {
            return ResponseEntity.status(HttpStatus.OK).body(charityInternalAPI.updateMyInfo(request));
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
            return ResponseEntity.status(HttpStatus.OK).body(charityInternalAPI.getInfo(userId));
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
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "order", defaultValue = "ascending", required = false) String order,
            @RequestParam(value = "filter", defaultValue = "companyName", required = false) String filter,
            @RequestParam(value = "keyword", defaultValue = "", required = false) String keyword) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(charityInternalAPI.getAll(pageNo, pageSize, order, filter, keyword));
        } catch (ResponseStatusException e) {
            // If the exception is a ResponseStatusException, return the status and message
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

    @GetMapping("/myInfo")
    ResponseEntity<Object> getMyInfo() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(charityInternalAPI.getMyInfo());
        } catch (ResponseStatusException e) {
            // If the exception is a ResponseStatusException, return the status and message
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }
}
