package com.charitan.profile.asset

import jakarta.annotation.security.RolesAllowed
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/profile/asset")
@Controller
class AssetController(
    private val assetService: AssetService,
) {
    @RolesAllowed("DONOR", "CHARITY")
    @PostMapping("/upload")
    fun getUploadSignedUrl(
        request: HttpServletRequest,
        @RequestBody fileName: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok(
            // add random number to invalidates cache (if exists)
            assetService.signedUploadUrl("${request.userPrincipal}/${(100000000..999999999).random()}-$fileName"),
        )
}
