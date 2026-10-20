package com.botanik.app.domain.repository

import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.ui.common.UiState
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    fun getAll(): Flow<UiState<List<CatalogPlant>>>
    fun getById(id: String): Flow<UiState<CatalogPlant>>
    fun search(query: String): Flow<UiState<List<CatalogPlant>>>

    /**
     * Indica si la última lectura del catálogo se resolvió con la copia
     * local (Room) por falta de conexión.
     *
     * Se usa para mostrarle al usuario que está viendo la copia guardada en
     * su dispositivo y no la versión de la nube.
     */
    val isServingFromCache: Flow<Boolean>
}
