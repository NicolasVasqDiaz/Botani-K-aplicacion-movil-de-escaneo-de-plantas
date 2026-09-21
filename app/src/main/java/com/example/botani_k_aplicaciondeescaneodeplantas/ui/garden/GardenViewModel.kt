package com.example.botani_k_aplicaciondeescaneodeplantas.ui.garden

import androidx.lifecycle.ViewModel
import com.example.botani_k_aplicaciondeescaneodeplantas.domain.model.Plant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class GardenUiState {
    data object Loading : GardenUiState()
    data class Loaded(val plants: List<Plant>) : GardenUiState()
    data class Error(val message: String) : GardenUiState()
}

@HiltViewModel
class GardenViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<GardenUiState>(GardenUiState.Loaded(emptyList()))
    val uiState: StateFlow<GardenUiState> = _uiState.asStateFlow()

    // TODO(Block 6): load from Room PlantDao via GetSavedPlantsUseCase.
    // Currently empty until Room/Firestore blocks land (see Handoff).
}
