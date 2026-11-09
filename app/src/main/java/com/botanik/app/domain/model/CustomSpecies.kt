package com.botanik.app.domain.model

/**
 * Especie identificada por el modelo on-device que no existe en el catálogo
 * global. Se guarda en el catálogo personal del usuario.
 */
data class CustomSpecies(
    val speciesId: String,
    val scientificName: String,
    val commonName: String,
    val family: String,
    val region: String,
    val floweringSeason: String,
    val care: Map<String, String>,
    val funFacts: List<String>,
    val imageUrl: String,
    val isEndemic: Boolean = false,
    val source: String = "ml_identification",
    val confidence: Float,
    val createdBy: String,
    val createdAt: Long
)