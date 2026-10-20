package com.botanik.app.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object CloudinaryUploadRequest {
    fun createImagePart(imageBytes: ByteArray, fileName: String): MultipartBody.Part {
        val requestBody = imageBytes.toRequestBody("image/*".toMediaType())
        return MultipartBody.Part.createFormData("file", fileName, requestBody)
    }

    fun createPresetPart(preset: String): RequestBody =
        preset.toRequestBody("text/plain".toMediaType())
}
