package com.botanik.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CloudinaryUploadResponse(
    @SerializedName("secure_url") val secureUrl: String,
    @SerializedName("public_id") val publicId: String,
    @SerializedName("delete_token") val deleteToken: String? = null,
    @SerializedName("format") val format: String? = null,
    @SerializedName("bytes") val bytes: Long? = null
)
