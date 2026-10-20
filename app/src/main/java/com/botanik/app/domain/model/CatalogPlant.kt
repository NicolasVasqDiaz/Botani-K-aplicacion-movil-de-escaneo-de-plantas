package com.botanik.app.domain.model

data class CatalogPlant(
    val id: String,
    val commonName: String,
    val scientificName: String,
    val family: String,
    val region: String,
    val floweringSeason: String,
    val care: Map<String, String>,
    val funFacts: List<String>,
    val imageUrl: String,
    val isEndemic: Boolean = false
)
