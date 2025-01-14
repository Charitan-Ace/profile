package com.charitan.profile.asset

interface AssetExternalService {
    fun signedUploadUrl(path: String): String

    fun signedObjectUrl(path: String): String
}
