package com.botanik.app.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Parser de las etiquetas del modelo PlantNet-300K ResNet18 (LiteRT).
 *
 * El modelo tiene 1081 salidas (índices 0..1080). El archivo
 * `plantnet_labels.json` (en `assets/`) mapea ID de especie → nombre científico.
 * Ordenamos por ID de especie para obtener el array ordenado que el modelo espera:
 * salida `i` → etiqueta `i` del array ordenado.
 *
 * Preprocesamiento esperado por el modelo (NO implementado aquí, se hará en 6B):
 *  - Input shape: [1, 3, 224, 224] (NCHW)
 *  - Color: RGB
 *  - Normalización ImageNet: mean [0.485, 0.456, 0.406], std [0.229, 0.224, 0.225]
 *  - Pre-procesado: center-crop → resize 224×224
 *  - Output shape: [1, 1081] logits
 */
object PlantNetLabels {

    private var cached: List<String>? = null

    /**
     * Carga y cachea las etiquetas desde `assets/plantnet_labels.json`.
     * Devuelve una lista ordenada cuyo índice coincide con la salida del modelo.
     */
    fun load(context: Context): List<String> {
        cached?.let { return it }
        val json = context.assets.open("plantnet_labels.json")
            .bufferedReader().use { it.readText() }
        val type = object : TypeToken<Map<String, String>>() {}.type
        val map: Map<String, String> = Gson().fromJson(json, type)
        val labels = map.entries
            .sortedBy { it.key.toIntOrNull() ?: Int.MAX_VALUE }
            .map { it.value }
        cached = labels
        return labels
    }

    /** Devuelve la etiqueta del índice dado, o null si está fuera de rango. */
    fun get(context: Context, index: Int): String? =
        load(context).getOrNull(index)
}
