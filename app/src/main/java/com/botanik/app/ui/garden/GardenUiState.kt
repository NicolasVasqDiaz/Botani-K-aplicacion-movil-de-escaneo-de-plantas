package com.botanik.app.ui.garden

import com.botanik.app.domain.model.GardenPlant
import com.botanik.app.ui.common.UiState

data class GardenScreenState(
    val plants: UiState<List<GardenPlant>> = UiState.Loading,
    val plantPendingDelete: GardenPlant? = null
)
