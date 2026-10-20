package com.botanik.app.ui.plantdetail

import com.botanik.app.domain.model.CatalogPlant

enum class FeedbackIssueType(val label: String) {
    WRONG_SPECIES("Especie identificada incorrectamente"),
    WRONG_HABITAT("Rango altitudinal / Hábitat impreciso"),
    WRONG_CARE("Consejo de cuidado o toxicidad")
}

data class PlantDetailUiState(
    val plant: CatalogPlant? = null,
    val userPhotoUrl: String? = null,
    val isLoading: Boolean = true,
    val isSavedToGarden: Boolean = false,
    val isCareExpanded: Boolean = false,
    val isCultureExpanded: Boolean = false,
    val showFeedbackSheet: Boolean = false,
    val errorMessage: String? = null,
    /** Indica que la ficha se está mostrando desde la caché local (sin conexión). */
    val isFromCache: Boolean = false
)
