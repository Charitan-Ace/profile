package com.charitan.profile.donor.internal;

import com.charitan.profile.asset.AssetService;
import com.charitan.profile.donor.internal.dtos.DonorDTO;
import com.charitan.profile.donor.internal.dtos.DonorSelfUpdateRequest;
import com.charitan.profile.donor.internal.dtos.DonorUpdateRequest;
import com.charitan.profile.jwt.internal.CustomUserDetails;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("api/profile/donor")
public class DonorController {
  private final DonorInternalAPI donorInternalAPI;
  private final AssetService assetService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  DonorController(DonorInternalAPI donorInternalAPI, AssetService assetService) {
    this.donorInternalAPI = donorInternalAPI;
    this.assetService = assetService;
  }

  @PatchMapping("/update")
  public ResponseEntity<DonorDTO> updateDonor(@RequestBody @Valid DonorUpdateRequest request) {
    try {
      var response = donorInternalAPI.updateDonor(request);
      return ResponseEntity.status(HttpStatus.OK).body(signAvatarUrl(response));
    } catch (ResponseStatusException e) {
      // If the exception is a ResponseStatusException, return the status and message
      logger.info("Error in updating donor #{}", request.getUserId(), e);
      return ResponseEntity.status(e.getStatusCode()).body(null);
    } catch (Exception e) {
      // Handle other exceptions
      logger.info("Error in updating donor #{}", request.getUserId(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @RolesAllowed({"DONOR"})
  @PatchMapping("/update/me")
  public ResponseEntity<DonorDTO> updateMyInfo(
      @RequestBody @Valid DonorSelfUpdateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    logger.info("Updating donor #{}", userDetails.getUserId());
    try {
      var response = donorInternalAPI.updateMyInfo(request, userDetails.getUserId());
      logger.info("Update donor #{} success!", userDetails.getUserId());
      return ResponseEntity.ok(signAvatarUrl(response));
    } catch (Exception e) {
      logger.info("Error in updating donor #{}", userDetails.getUserId(), e);
      return ResponseEntity.badRequest().body(null);
    }
  }

  @GetMapping("/info")
  public ResponseEntity<DonorDTO> getInfo(@RequestParam("id") UUID userId) {
    try {
      var info = donorInternalAPI.getInfo(userId);
      return ResponseEntity.status(HttpStatus.OK).body(signAvatarUrl(info));
    } catch (ResponseStatusException e) {
      // If the exception is a ResponseStatusException, return the status and message
      logger.error("Error while getting donor #{} info", userId, e);
      return ResponseEntity.status(e.getStatusCode()).body(null);
    } catch (Exception e) {
      // Handle other exceptions
      logger.error("Error while getting donor #{} info", userId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/all")
  ResponseEntity<Page<DonorDTO>> getAll(
      @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
      @RequestParam(value = "order", defaultValue = "ascending", required = false) String order,
      @RequestParam(value = "filter", defaultValue = "lastName", required = false) String filter,
      @RequestParam(value = "keyword", defaultValue = "", required = false) String keyword) {
    try {
      var info = donorInternalAPI.getAll(pageNo, pageSize, order, filter, keyword);
      return ResponseEntity.status(HttpStatus.OK).body(info.map(this::signAvatarUrl));
    } catch (ResponseStatusException e) {
      // If the exception is a ResponseStatusException, return the status and message
      logger.error("Error while getting donors info", e);
      return ResponseEntity.status(e.getStatusCode()).body(null);
    } catch (Exception e) {
      // Handle other exceptions
      logger.error("Error while getting donors info", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @RolesAllowed({"DONOR"})
  @GetMapping("/me")
  ResponseEntity<DonorDTO> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
    logger.info("Querying donor #{} info...", userDetails.getUserId());
    try {
      var info = donorInternalAPI.getInfo(userDetails.getUserId());
      return ResponseEntity.ok(signAvatarUrl(info));
    } catch (Exception e) {
      logger.error("Error while getting donor #{} info", userDetails.getUserId(), e);
      return ResponseEntity.internalServerError().body(null);
    }
  }

  private DonorDTO signAvatarUrl(DonorDTO dto) {
    if (!dto.getAssetsKey().isBlank()) {
      dto.setAssetsKey(
          assetService.signedObjectUrl(dto.getUserId().toString() + "/" + dto.getAssetsKey()));
    }
    return dto;
  }
}
