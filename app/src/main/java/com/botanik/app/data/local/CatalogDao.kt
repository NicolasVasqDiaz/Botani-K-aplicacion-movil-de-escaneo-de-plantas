package com.botanik.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/** DAO de la copia local del catálogo de plantas. */
@Dao
interface CatalogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entities: List<CatalogPlantEntity>)

    @Query("SELECT * FROM catalog_plants")
    suspend fun getAll(): List<CatalogPlantEntity>

    @Query("SELECT * FROM catalog_plants WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): CatalogPlantEntity?

    @Query("SELECT COUNT(*) FROM catalog_plants")
    suspend fun count(): Int

    @Query("DELETE FROM catalog_plants")
    suspend fun clear()

    /**
     * Reemplaza por completo la copia local del catálogo.
     *
     * Se hace en una transacción para que nunca quede el catálogo a medias:
     * primero se borra lo viejo y luego se inserta lo nuevo, o no se cambia nada.
     */
    @Transaction
    suspend fun replaceAll(entities: List<CatalogPlantEntity>) {
        clear()
        upsert(entities)
    }
}
