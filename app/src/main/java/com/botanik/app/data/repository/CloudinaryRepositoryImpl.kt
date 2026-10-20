package com.botanik.app.data.repository

import android.content.Context
import android.net.Uri
import com.botanik.app.BuildConfig
import com.botanik.app.data.remote.CloudinaryApiService
import com.botanik.app.data.remote.CloudinaryUploadRequest
import com.botanik.app.domain.repository.CloudinaryRepository
import com.botanik.app.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CloudinaryRepositoryImpl @Inject constructor(
    private val api: CloudinaryApiService
) : CloudinaryRepository {

    override suspend fun uploadImage(context: Context, uri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val bytes = ImageUtils.uriToCompressedByteArray(context, uri)
                val fileName = "plant_${System.currentTimeMillis()}.jpg"

                val response = api.uploadImage(
                    cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME,
                    file = CloudinaryUploadRequest.createImagePart(bytes, fileName),
                    preset = CloudinaryUploadRequest.createPresetPart(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                )
                Result.success(response.secureUrl)
            } catch (e: IOException) {
                Result.failure(Exception("Verifica tu conexión a internet"))
            } catch (e: HttpException) {
                val msg = when (e.code()) {
                    400 -> "Error al subir la imagen. Intenta de nuevo"
                    401, 403 -> "Error de configuración del servicio"
                    420, 429 -> "Demasiadas solicitudes. Espera un momento"
                    else -> "Algo salió mal. Intenta más tarde"
                }
                Result.failure(Exception(msg))
            } catch (e: Exception) {
                Result.failure(Exception("Algo salió mal. Intenta más tarde"))
            }
        }
}
