package com.example.botani_k_aplicaciondeescaneodeplantas.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.botani_k_aplicaciondeescaneodeplantas.domain.model.ConservationStatus
import com.example.botani_k_aplicaciondeescaneodeplantas.domain.model.Plant
import com.example.botani_k_aplicaciondeescaneodeplantas.domain.model.PlantCare
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ScanUiState {
    data object Idle : ScanUiState()
    data object BotThinking : ScanUiState()
    data class Identified(val plantId: String) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}

@HiltViewModel
class ScanViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun onScanCaptured() {
        viewModelScope.launch {
            _uiState.value = ScanUiState.BotThinking
            delay(2000)
            val mockPlant = mockPlantResult()
            _uiState.value = ScanUiState.Identified(mockPlant.id)
        }
    }

    fun resetState() {
        _uiState.value = ScanUiState.Idle
    }

    private fun mockPlantResult(): Plant = Plant(
        id = "cantuta_001",
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
}