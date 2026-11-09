package com.botanik.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plant_notes")
data class PlantNoteEntity(
    @PrimaryKey val noteId: String,
    val gardenPlantId: String,
    val text: String,
    val createdAt: Long
)