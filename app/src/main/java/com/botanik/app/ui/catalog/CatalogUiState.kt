package com.botanik.app.ui.catalog

import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.ui.common.UiState

enum class CatalogFilter(val label: String) {
    ALL("Todas"),
    ENDEMIC("Endémicas de Cajamarca"),
    JALCA("Flores de la Jalca"),
    TREES("Árboles"),
    MEDICINAL("Medicinales")
}

data class CatalogUiState(
    val searchQuery: String = "",
    val activeFilter: CatalogFilter = CatalogFilter.ALL,
    val plants: UiState<List<CatalogPlant>> = UiState.Loading,
    val plantCount: Int = 0,
    val isFromCache: Boolean = false,
    /** Región seleccionada en los chips dinámicos. null = "Todas". */
    val selectedRegion: String? = null,
    /** Familia seleccionada en los chips dinámicos. null = "Todas". */
    val selectedFamily: String? = null,
    /** Regiones disponibles (extraídas dinámicamente del catálogo). */
    val availableRegions: List<String> = emptyList(),
    /** Familias disponibles (extraídas dinámicamente del catálogo). */
    val availableFamilies: List<String> = emptyList()
)