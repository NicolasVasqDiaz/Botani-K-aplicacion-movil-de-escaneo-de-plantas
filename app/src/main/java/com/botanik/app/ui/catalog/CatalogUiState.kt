package com.botanik.app.ui.catalog

import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.ui.common.UiState

enum class CatalogFilter(val label: String) {
    ALL("Todas"),
    ENDEMIC("Endémicas de Cajamarca"),
    JALCA("Flores de la Jalca"),
    TREES("Árboles nativos"),
    MEDICINAL("Medicinales")
}

data class CatalogUiState(
    val searchQuery: String = "",
    val activeFilter: CatalogFilter = CatalogFilter.ALL,
    val plants: UiState<List<CatalogPlant>> = UiState.Loading,
    /**
     * Cantidad de plantas visibles en el catálogo.
     *
     * Qué:    número de plantas que el usuario está viendo ahora mismo.
     * Cómo:   se calcula en [CatalogViewModel] a partir de la lista ya
     *         filtrada (búsqueda por texto + filtro por categoría).
     * Por qué: se expone aquí para cumplir la regla de *state hoisting* del
     *         proyecto — la pantalla dibuja el texto a partir del estado,
     *         sin lógica de conteo dentro del Composable.
     */
    val plantCount: Int = 0,
    /** Indica que el catálogo se está mostrando desde la caché local (sin conexión). */
    val isFromCache: Boolean = false
)
