package com.botanik.app.domain.repository

import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.domain.model.CustomSpecies
import com.botanik.app.domain.model.GardenPlant
import com.botanik.app.ui.common.UiState
import kotlinx.coroutines.flow.Flow

interface GardenRepository {
    fun getGarden(): Flow<UiState<List<GardenPlant>>>
    fun isPlantInGarden(catalogPlantId: String): Flow<Boolean>

    suspend fun savePlant(
        catalogPlant: CatalogPlant,
        userPhotoUrl: String? = null,
        mlConfidence: Float? = null
    ): Result<Unit>

    suspend fun saveCustomPlant(
        species: CustomSpecies,
        userPhotoUrl: String,
        mlConfidence: Float? = null
    ): Result<Unit>

    suspend fun deletePlant(gardenPlantId: String): Result<Unit>
    suspend fun getSavedPlant(catalogPlantId: String): GardenPlant?

    suspend fun getGardenPlantById(gardenPlantId: String): GardenPlant?

    suspend fun correctGardenPlant(
        gardenPlantId: String,
        corrected: CatalogPlant
    ): Result<Unit>
}