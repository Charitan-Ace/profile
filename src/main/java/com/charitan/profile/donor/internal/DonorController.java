package com.charitan.profile.donor.internal;

import com.charitan.profile.donor.internal.dtos.DonorSelfUpdateRequest;
import com.charitan.profile.donor.internal.dtos.DonorUpdateRequest;
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
    final private DonorInternalAPI donorInternalAPI;

    DonorController(DonorInternalAPI donorInternalAPI) {
        this.donorInternalAPI = donorInternalAPI;
    }

    @PatchMapping("/update")
    public ResponseEntity<Object> updateDonor(@RequestBody @Valid DonorUpdateRequest request) {

        try {
            return ResponseEntity.status(HttpStatus.OK).body(donorInternalAPI.updateDonor(request));
        } catch (ResponseStatusException e) {
            // If the exception is a ResponseStatusException, return the status and message
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

    @PatchMapping("/update/me")
    public ResponseEntity<Object> updateMyInfo(@RequestBody @Valid DonorSelfUpdateRequest request) {

        try {
            return ResponseEntity.status(HttpStatus.OK).body(donorInternalAPI.updateMyInfo(request));
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
            return ResponseEntity.status(HttpStatus.OK).body(donorInternalAPI.getInfo(userId));
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
            @RequestParam(value = "filter", defaultValue = "lastName", required = false) String filter,
            @RequestParam(value = "keyword", defaultValue = "", required = false) String keyword) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(donorInternalAPI.getAll(pageNo, pageSize, order, filter, keyword));
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
            return ResponseEntity.status(HttpStatus.OK).body(donorInternalAPI.getMyInfo());
        } catch (ResponseStatusException e) {
            // If the exception is a ResponseStatusException, return the status and message
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }
}
