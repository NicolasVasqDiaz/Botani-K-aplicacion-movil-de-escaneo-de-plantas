package com.botanik.app.domain.repository

import android.content.Context
import android.net.Uri

interface CloudinaryRepository {
    /**
     * Sube la imagen apuntada por [uri] a Cloudinary.
     * Devuelve la URL segura (https://res.cloudinary.com/...).
     * Ejecuta en Dispatchers.IO.
     */
    suspend fun uploadImage(context: Context, uri: Uri): Result<String>
}
