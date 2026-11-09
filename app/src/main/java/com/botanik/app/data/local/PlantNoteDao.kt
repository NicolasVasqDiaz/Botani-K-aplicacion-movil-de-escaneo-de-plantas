package com.botanik.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantNoteDao {
    @Query("SELECT * FROM plant_notes WHERE gardenPlantId = :gardenPlantId ORDER BY createdAt DESC")
    fun getNotes(gardenPlantId: String): Flow<List<PlantNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: PlantNoteEntity)
}