package com.charitan.profile.asset

import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.http.Method
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.util.concurrent.TimeUnit

@RequestMapping("/api/profile/asset")
@Controller
class AssetController(
    private val minioClient: MinioClient,
) {
    @GetMapping("/upload/{name}")
    fun getUploadUrl(
        request: HttpServletRequest,
        @PathVariable name: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok(
            minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs
                    .builder()
                    .method(Method.PUT)
                    .bucket("charitan-bucket")
                    .`object`(name)
                    .expiry(2, TimeUnit.HOURS)
                    .build(),
            ),
        )
}
