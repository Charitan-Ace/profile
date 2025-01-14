package com.charitan.profile.asset

import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.http.Method
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class AssetService(
    private val minioClient: MinioClient,
) : AssetExternalService {
    override fun signedUploadUrl(path: String): String =
        minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs
                .builder()
                .method(Method.PUT)
                .bucket("charitan-bucket")
                .`object`(path)
                .expiry(30, TimeUnit.MINUTES)
                .build(),
        )

    override fun signedObjectUrl(path: String): String =
        minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs
                .builder()
                .method(Method.GET)
                .bucket("charitan-bucket")
                .`object`(path)
                .expiry(1, TimeUnit.DAYS)
                .build(),
        )
}
