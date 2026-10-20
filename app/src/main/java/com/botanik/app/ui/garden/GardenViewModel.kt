package com.botanik.app.ui.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botanik.app.domain.model.GardenPlant
import com.botanik.app.domain.repository.GardenRepository
import com.botanik.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GardenViewModel @Inject constructor(
    private val gardenRepository: GardenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GardenScreenState())
    val uiState: StateFlow<GardenScreenState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadGarden()
    }

    private fun loadGarden() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            gardenRepository.getGarden().collect { state ->
                _uiState.value = _uiState.value.copy(plants = state)
            }
        }
    }

    fun onDeleteClick(plant: GardenPlant) {
        _uiState.value = _uiState.value.copy(plantPendingDelete = plant)
    }

    fun onDismissDelete() {
        _uiState.value = _uiState.value.copy(plantPendingDelete = null)
    }

    fun onConfirmDelete() {
        val plant = _uiState.value.plantPendingDelete ?: return
        viewModelScope.launch {
            gardenRepository.deletePlant(plant.gardenPlantId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(plantPendingDelete = null)
                    _snackbarMessage.value = "Planta eliminada"
                },
                onFailure = { e ->
                    _snackbarMessage.value = e.message ?: "No pudimos eliminar la planta"
                }
            )
        }
    }

    fun onSnackbarShown() { _snackbarMessage.value = null }

    fun retry() = loadGarden()
}
