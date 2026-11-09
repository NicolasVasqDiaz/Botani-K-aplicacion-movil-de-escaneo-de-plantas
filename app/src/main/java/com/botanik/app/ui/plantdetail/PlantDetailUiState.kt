package com.botanik.app.ui.plantdetail

import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.domain.model.GardenPlant

data class PlantDetailUiState(
    val plant: CatalogPlant? = null,
    val userPhotoUrl: String? = null,
    val isLoading: Boolean = true,
    val isSavedToGarden: Boolean = false,
    val isCareExpanded: Boolean = false,
    val isCultureExpanded: Boolean = false,
    val errorMessage: String? = null,
    val isFromCache: Boolean = false,
    val mlConfidence: Float? = null,
    val gardenPlant: GardenPlant? = null,
    val isSubmittingFeedback: Boolean = false,
    val hasSubmittedFeedback: Boolean = false,
    val showCatalogPicker: Boolean = false,
    val catalogForPicker: List<CatalogPlant> = emptyList(),
    val feedbackErrorMessage: String? = null
)