package com.botanik.app.data.remote

import android.content.Context

/**
 * Carga los labels del modelo específico (botanik_labels.txt).
 * Formato del archivo: "0 cantuta\n1 molle\n..." — se extrae solo el nombre tras el espacio.
 */
object CatalogLabels {
    private var labels: List<String>? = null

    fun get(context: Context, index: Int): String? {
        if (labels == null) {
            labels = try {
                context.assets.open("botanik_labels.txt")
                    .bufferedReader()
                    .useLines { lines ->
                        lines.map { it.substringAfter(" ").trim() }.toList()
                    }
            } catch (e: Exception) {
                emptyList()
            }
        }
        return labels?.getOrNull(index)
    }
}