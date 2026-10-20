package com.botanik.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GardenDao {
    @Query("SELECT * FROM garden_plants ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<GardenPlantEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM garden_plants WHERE catalogPlantId = :catalogPlantId)")
    fun isPlantInGarden(catalogPlantId: String): Flow<Boolean>

    /**
     * Busca una planta guardada por su id de catálogo.
     *
     * Permite abrir la ficha de una planta de Mi Jardín sin conexión: la
     * entidad guarda todos los datos necesarios (nombres, familia, región,
     * floración, cuidados y curiosidades) para mostrarla en modo lectura.
     */
    @Query("SELECT * FROM garden_plants WHERE catalogPlantId = :catalogPlantId LIMIT 1")
    suspend fun findByCatalogPlantId(catalogPlantId: String): GardenPlantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GardenPlantEntity): Long

    @Query("DELETE FROM garden_plants WHERE gardenPlantId = :gardenPlantId")
    suspend fun deleteById(gardenPlantId: String)

    @Query("DELETE FROM garden_plants")
    suspend fun clear()
}
