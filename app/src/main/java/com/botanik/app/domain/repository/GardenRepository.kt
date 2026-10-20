package com.botanik.app.domain.repository

import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.domain.model.GardenPlant
import com.botanik.app.ui.common.UiState
import kotlinx.coroutines.flow.Flow

/**
 * Nota: savePlant recibe CatalogPlant (sin foto por ahora). La foto se añade
 * en Fase 4 (local) y Fase 5 (Cloudinary). Contrato temporal hasta Fase 4.
 */
interface GardenRepository {
    fun getGarden(): Flow<UiState<List<GardenPlant>>>
    fun isPlantInGarden(catalogPlantId: String): Flow<Boolean>
    suspend fun savePlant(catalogPlant: CatalogPlant, userPhotoUrl: String? = null): Result<Unit>
    suspend fun deletePlant(gardenPlantId: String): Result<Unit>

    /**
     * Obtiene una planta guardada del jardín (fuente local) para poder abrir
     * su ficha en modo lectura incluso sin conexión.
     *
     * @param catalogPlantId id de la planta en el catálogo.
     * @return la planta guardada o `null` si no está en el jardín local.
     */
    suspend fun getSavedPlant(catalogPlantId: String): GardenPlant?
}
