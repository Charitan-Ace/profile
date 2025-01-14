package com.charitan.profile.charity.internal;

import com.charitan.profile.asset.AssetExternalService;
import com.charitan.profile.charity.internal.dtos.CharityDTO;
import com.charitan.profile.charity.internal.dtos.CharitySelfUpdateRequest;
import com.charitan.profile.charity.internal.dtos.CharityUpdateRequest;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Validated
@RequestMapping("api/profile/charity")
public class CharityController {
  private final CharityInternalAPI charityInternalAPI;
  private final AssetExternalService assetService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  CharityController(CharityInternalAPI charityInternalAPI, AssetExternalService assetService) {
    this.charityInternalAPI = charityInternalAPI;
    this.assetService = assetService;
  }

  @PatchMapping("/update")
  public ResponseEntity<CharityDTO> updateDonor(@RequestBody @Valid CharityUpdateRequest request) {
    logger.info("Updating charity #{}", request.getUserId());
    try {
      var response = charityInternalAPI.updateCharity(request);
      logger.info("Update charity #{} success!", request.getUserId());
      return ResponseEntity.status(HttpStatus.OK).body(signDtoAsset(response));
    } catch (ResponseStatusException e) {
      // If the exception is a ResponseStatusException, return the status and message
      logger.info("Error in updating charity #{}", request.getUserId(), e);
      return ResponseEntity.status(e.getStatusCode()).body(null);
    } catch (Exception e) {
      // Handle other exceptions
      logger.info("Error in updating charity #{}", request.getUserId(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @RolesAllowed({"CHARITY"})
  @PatchMapping("/update/me")
  public ResponseEntity<CharityDTO> updateMyInfo(
      @RequestBody @Valid CharitySelfUpdateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    logger.info("Updating charity #{}", userDetails.getUserId());
    try {
      var response = charityInternalAPI.updateMyInfo(request, userDetails.getUserId());
      logger.info("Update charity #{} success!", userDetails.getUserId());
      return ResponseEntity.ok(signDtoAsset(response));
    } catch (Exception e) {
      logger.info("Error in updating charity #{}", userDetails.getUserId(), e);
      return ResponseEntity.badRequest().body(null);
    }
  }

  @GetMapping("/info")
  public ResponseEntity<CharityDTO> getInfo(@RequestParam("id") UUID userId) {
    logger.info("Querying charity #{} info", userId);
    try {
      var response = charityInternalAPI.getInfo(userId);
      return ResponseEntity.status(HttpStatus.OK).body(signDtoAsset(response));
    } catch (ResponseStatusException e) {
      // If the exception is a ResponseStatusException, return the status and message
      logger.error("Error while getting charity #{} info", userId, e);
      return ResponseEntity.status(e.getStatusCode()).body(null);
    } catch (Exception e) {
      // Handle other exceptions
      logger.error("Error while getting charity #{} info", userId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @GetMapping("/all")
  ResponseEntity<Page<CharityDTO>> getAll(
      @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
      @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
      @RequestParam(value = "order", defaultValue = "ascending", required = false) String order,
      @RequestParam(value = "filter", defaultValue = "companyName", required = false) String filter,
      @RequestParam(value = "keyword", defaultValue = "", required = false) String keyword) {
    logger.info("Querying charities info");
    try {
      var response = charityInternalAPI.getAll(pageNo, pageSize, order, filter, keyword);
      return ResponseEntity.status(HttpStatus.OK).body(response.map(this::signDtoAsset));
    } catch (ResponseStatusException e) {
      // If the exception is a ResponseStatusException, return the status and message
      logger.error("Error while getting charities info", e);
      return ResponseEntity.status(e.getStatusCode()).body(null);
    } catch (Exception e) {
      // Handle other exceptions
      logger.error("Error while getting charities info", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
  }

  @RolesAllowed({"CHARITY"})
  @GetMapping("/me")
  ResponseEntity<CharityDTO> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
    logger.info("Querying charity #{} info", userDetails.getUserId());
    try {
      var info = charityInternalAPI.getInfo(userDetails.getUserId());
      return ResponseEntity.ok(signDtoAsset(info));
    } catch (Exception e) {
      logger.error("Error while getting charity #{} info", userDetails.getUserId(), e);
      return ResponseEntity.internalServerError().body(null);
    }
  }

  private CharityDTO signDtoAsset(CharityDTO dto) {
    if (dto.getVideo() != null && !dto.getVideo().isBlank()) {
      dto.setVideo(assetService.signedObjectUrl(dto.getUserId().toString() + "/" + dto.getVideo()));
    }
    if (dto.getAssetsKey() != null && !dto.getAssetsKey().isBlank()) {
      dto.setAssetsKey(
          assetService.signedObjectUrl(dto.getUserId().toString() + "/" + dto.getAssetsKey()));
    }
    return dto;
  }
}
