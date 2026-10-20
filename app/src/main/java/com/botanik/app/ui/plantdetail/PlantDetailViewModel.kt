package com.botanik.app.ui.plantdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.domain.repository.CatalogRepository
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
class PlantDetailViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val gardenRepository: GardenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlantDetailUiState())
    val uiState: StateFlow<PlantDetailUiState> = _uiState.asStateFlow()

    private var isInGardenJob: Job? = null

    /**
     * Carga la ficha de una planta.
     *
     * Qué:    muestra la ficha de la especie seleccionada.
     * Cómo:   primero intenta el catálogo en Firestore; si la red falla,
     *         recurre a la copia local de Mi Jardín (Room), que contiene
     *         todos los datos necesarios para mostrarla.
     * Por qué: cumple el requisito de "detalle de planta guardada
     *         disponible también sin conexión mediante caché local" (RU-07).
     */
    fun loadPlantById(plantId: String, userPhotoUrl: String?) {
        _uiState.value = _uiState.value.copy(isLoading = true, userPhotoUrl = userPhotoUrl)
        viewModelScope.launch {
            catalogRepository.getById(plantId).collect { state ->
                when (state) {
                    is UiState.Success -> {
                        _uiState.value = _uiState.value.copy(plant = state.data, isLoading = false, errorMessage = null)
                        observeIsInGarden(state.data.id)
                    }
                    is UiState.Error -> {
                        loadFromLocalCache(plantId)
                    }
                    else -> { /* Loading / Empty: mantener isLoading */ }
                }
            }
        }
    }

    /**
     * Fallback offline: reconstruye la planta desde la copia local del jardín.
     *
     * Si la planta está en la caché de Room se muestra en modo lectura; si
     * tampoco está, se informa el error original de red.
     */
    private suspend fun loadFromLocalCache(plantId: String) {
        val cached = gardenRepository.getSavedPlant(plantId)
        if (cached != null) {
            _uiState.value = _uiState.value.copy(
                plant = cached.toCatalogPlant(),
                isLoading = false,
                errorMessage = null,
                isFromCache = true
            )
            observeIsInGarden(cached.catalogPlantId)
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "No pudimos cargar la planta. Verifica tu conexión."
            )
        }
    }

    /** Convierte una planta guardada del jardín al modelo de catálogo para la ficha. */
    private fun com.botanik.app.domain.model.GardenPlant.toCatalogPlant(): CatalogPlant =
        CatalogPlant(
            id = catalogPlantId,
            commonName = commonName,
            scientificName = scientificName,
            family = family,
            region = region,
            floweringSeason = floweringSeason,
            care = care,
            funFacts = funFacts,
            imageUrl = userPhotoUrl.orEmpty(),
            isEndemic = false
        )

    private fun observeIsInGarden(catalogPlantId: String) {
        isInGardenJob?.cancel()
        isInGardenJob = viewModelScope.launch {
            gardenRepository.isPlantInGarden(catalogPlantId).collect { isSaved ->
                _uiState.value = _uiState.value.copy(isSavedToGarden = isSaved)
            }
        }
    }

    fun toggleCareExpanded() {
        _uiState.value = _uiState.value.copy(isCareExpanded = !_uiState.value.isCareExpanded)
    }

    fun toggleCultureExpanded() {
        _uiState.value = _uiState.value.copy(isCultureExpanded = !_uiState.value.isCultureExpanded)
    }

    /** Persiste la planta actual en Mi Jardín (Room + Firestore) vía [GardenRepository]. */
    fun saveToGarden() {
        val plant = _uiState.value.plant ?: return
        val userPhotoUrl = _uiState.value.userPhotoUrl
        viewModelScope.launch {
            gardenRepository.savePlant(plant, userPhotoUrl).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSavedToGarden = true, errorMessage = null)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = e.message ?: "No pudimos guardar la planta. Intenta de nuevo"
                    )
                }
            )
        }
    }

    fun toggleFeedbackSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showFeedbackSheet = show)
    }

    fun submitFeedback(issue: FeedbackIssueType) {
        _uiState.value = _uiState.value.copy(showFeedbackSheet = false)
    }

    fun onErrorShown() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
