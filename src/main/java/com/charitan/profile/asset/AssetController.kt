package com.charitan.profile.asset

import com.charitan.profile.jwt.internal.CustomUserDetails
import jakarta.annotation.security.RolesAllowed
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/profile/asset")
@Controller
class AssetController(
    private val assetService: AssetExternalService,
) {
    @RolesAllowed("DONOR", "CHARITY")
    @PostMapping("/upload")
    fun getUploadSignedUrl(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody fileName: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok(
            assetService.signedUploadUrl(
                "${userDetails.userId}/$fileName",
            ),
        )

    @RolesAllowed("ADMIN")
    @PostMapping("/upload/{id}")
    fun getUploadSignedUrl(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable id: String,
        @RequestBody fileName: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok(
            assetService.signedUploadUrl(
                "$id/$fileName",
            ),
        )
}
