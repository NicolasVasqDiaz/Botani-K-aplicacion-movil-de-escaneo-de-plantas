package com.botanik.app.domain.model

data class PlantNote(
    val noteId: String,
    val gardenPlantId: String,
    val text: String,
    val createdAt: Long
)