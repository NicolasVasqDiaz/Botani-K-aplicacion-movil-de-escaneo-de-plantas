package com.botanik.app.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * Identificador de plantas on-device usando el modelo PlantNet-300K ResNet18 (LiteRT).
 *
 * Preprocesamiento (según Bloque 6A):
 *  - Input: [1, 3, 224, 224] (NCHW), RGB, float32
 *  - Normalización ImageNet: mean [0.485, 0.456, 0.406], std [0.229, 0.224, 0.225]
 *  - Pre-procesado: center-crop → resize 224×224
 *  - Output: [1, 1081] logits
 *
 * Nota: Se usa CPU (sin GPU delegate) porque litert-gpu requiere Kotlin 2.4+
 *       y el proyecto usa Kotlin 2.2.0. El GPU delegate se añadirá cuando
 *       se actualice Kotlin.
 *
 * Uso:
 *  1. Llamar [identify] con el Uri de la imagen.
 *  2. Devuelve el nombre científico de la especie con mayor probabilidad.
 */
@Singleton
class PlantIdentifier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var model: CompiledModel? = null

    /**
     * Carga el modelo de forma lazy. Devuelve null si el modelo no está disponible
     * (por ejemplo, en una máquina donde no se descargó el archivo).
     */
    private fun getModel(): CompiledModel? {
        if (model != null) return model
        return try {
            CompiledModel.create(context.assets, "plantnet.tflite")
        } catch (e: Exception) {
            null
        }.also { model = it }
    }

    /**
     * Identifica la planta de la imagen dada.
     *
     * @param imageUri Uri de la imagen a identificar.
     * @return El nombre científico de la especie más probable, o null si falla.
     */
    fun identify(imageUri: Uri): String? {
        val compiled = getModel() ?: return null
        val bitmap = loadBitmap(imageUri) ?: return null
        val input = preprocess(bitmap)
        return try {
            val inputBuffers = compiled.createInputBuffers()
            inputBuffers[0].writeFloat(input)
            val outputBuffers = compiled.run(inputBuffers)
            val logits = outputBuffers[0].readFloat()
            val topIndex = logits.indices.maxByOrNull { logits[it] } ?: return null
            PlantNetLabels.get(context, topIndex)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Carga un Bitmap desde un Uri.
     */
    private fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Preprocesa el bitmap: center-crop → resize 224×224 → NCHW float array normalizado.
     */
    private fun preprocess(bitmap: Bitmap): FloatArray {
        val size = 224
        // Center-crop to square
        val minSide = min(bitmap.width, bitmap.height)
        val x = (bitmap.width - minSide) / 2
        val y = (bitmap.height - minSide) / 2
        val cropped = Bitmap.createBitmap(bitmap, x, y, minSide, minSide)

        // Resize to 224×224
        val resized = Bitmap.createScaledBitmap(cropped, size, size, true)

        // Convert to NCHW float array normalizado (ImageNet)
        val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
        val std = floatArrayOf(0.229f, 0.224f, 0.225f)
        val pixels = IntArray(size * size)
        resized.getPixels(pixels, 0, size, 0, 0, size, size)

        val floatArray = FloatArray(3 * size * size)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = ((pixel shr 16) and 0xFF) / 255f
            val g = ((pixel shr 8) and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f

            // NCHW: channel 0 = R, channel 1 = G, channel 2 = B
            floatArray[0 * size * size + i] = (r - mean[0]) / std[0]
            floatArray[1 * size * size + i] = (g - mean[1]) / std[1]
            floatArray[2 * size * size + i] = (b - mean[2]) / std[2]
        }

        // Cleanup
        if (cropped != bitmap) cropped.recycle()
        if (resized != cropped) resized.recycle()

        return floatArray
    }
}