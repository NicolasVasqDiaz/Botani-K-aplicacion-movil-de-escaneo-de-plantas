package com.botanik.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

object ImageUtils {
    /**
     * Convierte un [Uri] de imagen a [ByteArray] JPEG comprimido.
     * Itera la calidad hasta quedar por debajo de [maxSizeKb] (o hasta 40%).
     * Lanza IllegalStateException si no puede abrir el Uri.
     */
    fun uriToCompressedByteArray(
        context: Context,
        uri: Uri,
        maxSizeKb: Int = 800
    ): ByteArray {
        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: throw IllegalStateException("No se pudo abrir la imagen seleccionada")

        return compressIteratively(bitmap, maxSizeKb)
    }

    private fun compressIteratively(bitmap: Bitmap, maxSizeKb: Int): ByteArray {
        var quality = 90
        var bytes: ByteArray
        val maxBytes = maxSizeKb * 1024
        do {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            bytes = stream.toByteArray()
            quality -= 10
        } while (bytes.size > maxBytes && quality >= 40)
        return bytes
    }
}
