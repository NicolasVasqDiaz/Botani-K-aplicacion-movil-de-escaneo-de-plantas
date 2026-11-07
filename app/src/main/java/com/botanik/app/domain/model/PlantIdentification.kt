package com.botanik.app.domain.model

/**
 * Resultado de una identificación de planta por el modelo on-device.
 *
 * @property scientificName Nombre científico devuelto por el modelo.
 * @property confidence Probabilidad softmax (0.0 a 1.0).
 * @property rank Posición en el top-K (0 = mejor candidato).
 */
data class PlantIdentification(
    val scientificName: String,
    val confidence: Float,
    val rank: Int
)