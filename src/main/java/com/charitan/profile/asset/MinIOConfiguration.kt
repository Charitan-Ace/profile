package com.charitan.profile.asset

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class MinIOConfiguration(
    @Value("\${s3.endpoint}")
    val endpoint: String,
    @Value("\${s3.key.access}")
    val access: String,
    @Value("\${s3.key.secret}")
    val secret: String,
) {
    @Bean
    open fun minioClient(): MinioClient =
        MinioClient
            .builder()
            .endpoint(endpoint)
            .credentials(access, secret)
            .build()
}
