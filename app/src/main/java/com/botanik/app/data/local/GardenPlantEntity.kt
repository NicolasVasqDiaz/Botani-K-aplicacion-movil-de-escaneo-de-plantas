package com.botanik.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "garden_plants")
data class GardenPlantEntity(
    @PrimaryKey val gardenPlantId: String,
    val catalogPlantId: String,
    val commonName: String,
    val scientificName: String,
    val family: String,
    val region: String,
    val floweringSeason: String,
    val careJson: String,
    val funFactsJson: String,
    val userPhotoUrl: String?,
    val savedAt: Long
)
