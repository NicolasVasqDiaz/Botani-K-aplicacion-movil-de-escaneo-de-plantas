package com.botanik.app.domain.model

data class GardenPlant(
    val gardenPlantId: String,
    val catalogPlantId: String,
    val commonName: String,
    val scientificName: String,
    val family: String,
    val region: String,
    val floweringSeason: String,
    val care: Map<String, String>,
    val funFacts: List<String>,
    val userPhotoUrl: String?,
    val savedAt: Long
)
