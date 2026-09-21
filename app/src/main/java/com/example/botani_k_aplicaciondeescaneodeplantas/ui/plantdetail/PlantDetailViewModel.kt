package com.example.botani_k_aplicaciondeescaneodeplantas.ui.plantdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.botani_k_aplicaciondeescaneodeplantas.domain.model.ConservationStatus
import com.example.botani_k_aplicaciondeescaneodeplantas.domain.model.Plant
import com.example.botani_k_aplicaciondeescaneodeplantas.domain.model.PlantCare
import com.example.botani_k_aplicaciondeescaneodeplantas.ui.navigation.BotaniKRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FeedbackStatus { ACCURATE, DISCREPANCY }

data class PlantDetailUiState(
    val plant: Plant? = null,
    val isLoading: Boolean = true,
    val isSavedToGarden: Boolean = false,
    val feedbackSubmitted: FeedbackStatus? = null,
    val isCareExpanded: Boolean = false,
    val isCultureExpanded: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PlantDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val plantId: String = checkNotNull(savedStateHandle[BotaniKRoute.PlantDetail.ARG_PLANT_ID])

    private val _uiState = MutableStateFlow(PlantDetailUiState())
    val uiState: StateFlow<PlantDetailUiState> = _uiState.asStateFlow()

    init { loadPlant() }

    private fun loadPlant() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val plant = Plant(
                id = plantId,
                commonName = "Cantuta",
                scientificName = "Cantua buxifolia",
                family = "Polemoniaceae",
                habitat = "2,800 - 3,800 m s.n.m.",
                floweringSeason = "Abril - Julio",
                conservationStatus = ConservationStatus.LEAST_CONCERN,
                aiConfidence = 0.987f,
                imageUrl = "",
                care = PlantCare(
                    watering = "Moderado, cada 3-4 días",
                    sunlight = "Pleno sol",
                    soil = "Bien drenado, ligeramente ácido"
                ),
                culturalFacts = "Conocida como la 'flor sagrada de los incas', usada en ceremonias andinas.",
                botFact = "¡Solo crece en los valles altoandinos de Cajamarca!"
            )
            _uiState.value = _uiState.value.copy(plant = plant, isLoading = false)
        }
    }

    fun toggleCareExpanded() {
        _uiState.value = _uiState.value.copy(isCareExpanded = !_uiState.value.isCareExpanded)
    }

    fun toggleCultureExpanded() {
        _uiState.value = _uiState.value.copy(isCultureExpanded = !_uiState.value.isCultureExpanded)
    }

    fun submitFeedback(status: FeedbackStatus) {
        _uiState.value = _uiState.value.copy(feedbackSubmitted = status)
    }

    fun saveToGarden() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavedToGarden = true)
        }
    }
}