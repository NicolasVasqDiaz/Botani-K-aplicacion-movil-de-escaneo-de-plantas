package com.example.botani_k_aplicaciondeescaneodeplantas.domain.model

data class Plant(
    val id: String,
    val commonName: String,
    val scientificName: String,
    val family: String,
    val habitat: String,
    val floweringSeason: String,
    val conservationStatus: ConservationStatus,
    val aiConfidence: Float,
    val imageUrl: String,
    val care: PlantCare,
    val culturalFacts: String,
    val botFact: String
)

enum class ConservationStatus(val code: String, val label: String) {
    CRITICALLY_ENDANGERED("CR", "En Peligro Crítico"),
    ENDANGERED("EN", "En Peligro"),
    VULNERABLE("VU", "Vulnerable"),
    LEAST_CONCERN("LC", "Preocupación Menor"),
    DATA_DEFICIENT("DD", "Datos Insuficientes")
}

data class PlantCare(
    val watering: String,
    val sunlight: String,
    val soil: String
)