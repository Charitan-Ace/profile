package com.charitan.profile.charity.internal;

import com.charitan.profile.charity.internal.dtos.CharitySelfUpdateRequest;
import com.charitan.profile.charity.internal.dtos.CharityUpdateRequest;
import com.charitan.profile.jwt.internal.CustomUserDetails;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@Validated
@RequestMapping("api/profile/charity")
public class CharityController {
    final private CharityInternalAPI charityInternalAPI;

    CharityController(CharityInternalAPI charityInternalAPI) {
        this.charityInternalAPI = charityInternalAPI;
    }

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

    @RolesAllowed({"CHARITY"})
    @PatchMapping("/update/me")
    public ResponseEntity<Object> updateMyInfo(@RequestBody @Valid CharitySelfUpdateRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        var response = charityInternalAPI.updateMyInfo(request, userDetails.getUserId());

        return ResponseEntity.ok(response);
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

    @RolesAllowed({"CHARITY"})
    @GetMapping("/me")
    ResponseEntity<Object> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        System.out.println(userDetails.getUserId());
        var info = charityInternalAPI.getInfo(userDetails.getUserId());

        return ResponseEntity.ok(info);
    }
}
