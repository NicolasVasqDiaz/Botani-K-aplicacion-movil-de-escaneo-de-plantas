package com.botanik.app.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.botanik.app.domain.model.PlantIdentification
import com.google.ai.edge.litert.CompiledModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.min

/**
 * Identificador de plantas on-device con dos modelos:
 *  - Específico (botanik_catalog.tflite): 21 especies del catálogo, NHWC, normalización [0,1]
 *  - Global (plantnet.tflite): 1081 especies, NCHW, normalización ImageNet
 *
 * Estrategia: usar el específico primero; si confianza < threshold → fallback al global.
 */
@Singleton
class PlantIdentifier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var catalogModel: CompiledModel? = null
    private var globalModel: CompiledModel? = null

    private fun getCatalogModel(): CompiledModel? {
        if (catalogModel != null) return catalogModel
        return try {
            CompiledModel.create(context.assets, "botanik_catalog.tflite")
        } catch (e: Exception) {
            null
        }.also { catalogModel = it }
    }

    private fun getGlobalModel(): CompiledModel? {
        if (globalModel != null) return globalModel
        return try {
            CompiledModel.create(context.assets, "plantnet.tflite")
        } catch (e: Exception) {
            null
        }.also { globalModel = it }
    }

    /**
     * Identifica la planta. Primero intenta con el modelo específico del catálogo.
     * Si la confianza del top-1 < [CATALOG_CONFIDENCE_THRESHOLD], usa el modelo global.
     */
    fun identify(imageUri: Uri, topK: Int = 3): List<PlantIdentification>? {
        val bitmap = loadBitmap(imageUri) ?: return null

        val catalogResult = identifyWithCatalogModel(bitmap, topK)
        if (catalogResult != null && catalogResult.isNotEmpty()) {
            if (catalogResult.first().confidence >= CATALOG_CONFIDENCE_THRESHOLD) {
                return catalogResult
            }
        }

        return identifyWithGlobalModel(bitmap, topK)
    }

    private fun identifyWithCatalogModel(bitmap: Bitmap, topK: Int): List<PlantIdentification>? {
        val model = getCatalogModel() ?: return null
        return try {
            val input = preprocessNHWC(bitmap)
            val inputBuffers = model.createInputBuffers()
            inputBuffers[0].writeFloat(input)
            val outputBuffers = model.run(inputBuffers)
            val probabilities = outputBuffers[0].readFloat() // ya tiene softmax

            val topIndices = probabilities.indices
                .sortedByDescending { probabilities[it] }
                .take(topK)

            topIndices.mapIndexed { rank, index ->
                PlantIdentification(
                    scientificName = CatalogLabels.get(context, index) ?: "Desconocido",
                    confidence = probabilities[index],
                    rank = rank
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun identifyWithGlobalModel(bitmap: Bitmap, topK: Int): List<PlantIdentification>? {
        val model = getGlobalModel() ?: return null
        return try {
            val input = preprocessNCHW(bitmap)
            val inputBuffers = model.createInputBuffers()
            inputBuffers[0].writeFloat(input)
            val outputBuffers = model.run(inputBuffers)
            val logits = outputBuffers[0].readFloat()

            val probabilities = softmax(logits)
            val topIndices = probabilities.indices
                .sortedByDescending { probabilities[it] }
                .take(topK)

            topIndices.mapIndexed { rank, index ->
                PlantIdentification(
                    scientificName = PlantNetLabels.get(context, index) ?: "Desconocido",
                    confidence = probabilities[index],
                    rank = rank
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    /** NHWC [1, 224, 224, 3], normalizado a [0,1]. Para el modelo específico. */
    /** NHWC [1, 224, 224, 3], normalizado a [-1,1] (Teachable Machine / MobileNet). */
    private fun preprocessNHWC(bitmap: Bitmap): FloatArray {
        val size = 224
        // Resize directo SIN center-crop (Teachable Machine no usa crop)
        val resized = Bitmap.createScaledBitmap(bitmap, size, size, true)

        val pixels = IntArray(size * size)
        resized.getPixels(pixels, 0, size, 0, 0, size, size)

        val floatArray = FloatArray(size * size * 3)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            // Normalización [-1, 1]: (x / 127.5) - 1
            val r = (((pixel shr 16) and 0xFF) / 127.5f) - 1f
            val g = (((pixel shr 8) and 0xFF) / 127.5f) - 1f
            val b = ((pixel and 0xFF) / 127.5f) - 1f
            floatArray[i * 3 + 0] = r
            floatArray[i * 3 + 1] = g
            floatArray[i * 3 + 2] = b
        }

        if (resized != bitmap) resized.recycle()
        return floatArray
    }

    /** NCHW [1, 3, 224, 224], normalización ImageNet. Para el modelo global. */
    private fun preprocessNCHW(bitmap: Bitmap): FloatArray {
        val size = 224
        val minSide = min(bitmap.width, bitmap.height)
        val x = (bitmap.width - minSide) / 2
        val y = (bitmap.height - minSide) / 2
        val cropped = Bitmap.createBitmap(bitmap, x, y, minSide, minSide)
        val resized = Bitmap.createScaledBitmap(cropped, size, size, true)

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
            floatArray[0 * size * size + i] = (r - mean[0]) / std[0]
            floatArray[1 * size * size + i] = (g - mean[1]) / std[1]
            floatArray[2 * size * size + i] = (b - mean[2]) / std[2]
        }

        if (cropped != bitmap) cropped.recycle()
        if (resized != cropped) resized.recycle()

        return floatArray
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: return FloatArray(logits.size) { 0f }
        val exps = DoubleArray(logits.size) { exp((logits[it] - maxLogit).toDouble()) }
        val sumExps = exps.sum()
        return FloatArray(logits.size) { (exps[it] / sumExps).toFloat() }
    }

    private fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        /** Si el modelo específico da < 0.50, usamos el global. */
        private const val CATALOG_CONFIDENCE_THRESHOLD = 0.50f
    }
}