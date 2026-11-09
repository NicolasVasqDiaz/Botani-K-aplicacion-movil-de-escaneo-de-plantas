package com.botanik.app.domain.model

/**
 * Feedback del usuario sobre una identificación de planta.
 * Se guarda en users/{uid}/feedback/{feedbackId}.
 */
data class PlantFeedback(
    val feedbackId: String,
    val uid: String,
    val gardenPlantId: String,
    val originalScientificName: String,
    val originalConfidence: Float,
    val isCorrect: Boolean,
    val correctedCatalogPlantId: String?,
    val correctedScientificName: String?,
    val createdAt: Long
)