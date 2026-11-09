package com.botanik.app.ui.plantdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.domain.model.PlantFeedback
import com.botanik.app.domain.repository.CatalogRepository
import com.botanik.app.domain.repository.FeedbackRepository
import com.botanik.app.domain.repository.GardenRepository
import com.botanik.app.ui.common.UiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlantDetailViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val gardenRepository: GardenRepository,
    private val feedbackRepository: FeedbackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlantDetailUiState())
    val uiState: StateFlow<PlantDetailUiState> = _uiState.asStateFlow()

    private var isInGardenJob: Job? = null

    fun loadPlantById(plantId: String, userPhotoUrl: String?, mlConfidence: Float? = null) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            userPhotoUrl = userPhotoUrl,
            mlConfidence = mlConfidence
        )
        viewModelScope.launch {
            catalogRepository.getById(plantId).collect { state ->
                when (state) {
                    is UiState.Success -> {
                        _uiState.value = _uiState.value.copy(
                            plant = state.data,
                            isLoading = false,
                            errorMessage = null
                        )
                        observeGardenPlant(state.data.id)
                    }
                    is UiState.Error -> loadFromLocalCache(plantId)
                    else -> { }
                }
            }
        }
    }

    private suspend fun loadFromLocalCache(plantId: String) {
        val cached = gardenRepository.getSavedPlant(plantId)
        if (cached != null) {
            _uiState.value = _uiState.value.copy(
                plant = cached.toCatalogPlant(),
                isLoading = false,
                errorMessage = null,
                isFromCache = true,
                gardenPlant = cached
            )
            observeGardenPlant(cached.catalogPlantId)
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "No pudimos cargar la planta. Verifica tu conexión."
            )
        }
    }

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

    private fun observeGardenPlant(catalogPlantId: String) {
        isInGardenJob?.cancel()
        isInGardenJob = viewModelScope.launch {
            gardenRepository.isPlantInGarden(catalogPlantId).collect { isSaved ->
                _uiState.value = _uiState.value.copy(isSavedToGarden = isSaved)
                if (isSaved) {
                    val gardenPlant = gardenRepository.getSavedPlant(catalogPlantId)
                    _uiState.value = _uiState.value.copy(gardenPlant = gardenPlant)
                    if (gardenPlant != null) {
                        feedbackRepository.getFeedbackForPlant(gardenPlant.gardenPlantId).fold(
                            onSuccess = { existing ->
                                if (existing != null) {
                                    _uiState.value = _uiState.value.copy(hasSubmittedFeedback = true)
                                }
                            },
                            onFailure = { }
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(gardenPlant = null)
                }
            }
        }
    }

    fun toggleCareExpanded() {
        _uiState.value = _uiState.value.copy(isCareExpanded = !_uiState.value.isCareExpanded)
    }

    fun toggleCultureExpanded() {
        _uiState.value = _uiState.value.copy(isCultureExpanded = !_uiState.value.isCultureExpanded)
    }

    fun saveToGarden() {
        val plant = _uiState.value.plant ?: return
        val userPhotoUrl = _uiState.value.userPhotoUrl
        val mlConfidence = _uiState.value.mlConfidence
        viewModelScope.launch {
            gardenRepository.savePlant(plant, userPhotoUrl, mlConfidence).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSavedToGarden = true, errorMessage = null)
                    observeGardenPlant(plant.id)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = e.message ?: "No pudimos guardar la planta. Intenta de nuevo"
                    )
                }
            )
        }
    }

    fun submitPositiveFeedback() {
        val gardenPlant = _uiState.value.gardenPlant ?: return
        val confidence = gardenPlant.mlConfidence ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmittingFeedback = true,
                feedbackErrorMessage = null
            )
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                _uiState.value = _uiState.value.copy(
                    isSubmittingFeedback = false,
                    feedbackErrorMessage = "Sesión no iniciada"
                )
                return@launch
            }
            val feedback = PlantFeedback(
                feedbackId = UUID.randomUUID().toString(),
                uid = uid,
                gardenPlantId = gardenPlant.gardenPlantId,
                originalScientificName = gardenPlant.scientificName,
                originalConfidence = confidence,
                isCorrect = true,
                correctedCatalogPlantId = null,
                correctedScientificName = null,
                createdAt = System.currentTimeMillis()
            )
            feedbackRepository.submitFeedback(feedback).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSubmittingFeedback = false,
                        hasSubmittedFeedback = true
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isSubmittingFeedback = false,
                        feedbackErrorMessage = e.message ?: "Error al guardar feedback"
                    )
                }
            )
        }
    }

    fun onNegativeFeedbackClick() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingFeedback = true)
            val state = catalogRepository.getAll().first { it !is UiState.Loading }
            val plants = (state as? UiState.Success)?.data ?: emptyList()
            _uiState.value = _uiState.value.copy(
                showCatalogPicker = true,
                catalogForPicker = plants,
                isSubmittingFeedback = false
            )
        }
    }

    fun onCorrectSpeciesSelected(corrected: CatalogPlant) {
        val gardenPlant = _uiState.value.gardenPlant ?: return
        val confidence = gardenPlant.mlConfidence ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmittingFeedback = true,
                feedbackErrorMessage = null
            )
            val result = gardenRepository.correctGardenPlant(gardenPlant.gardenPlantId, corrected)
            if (result.isSuccess) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val feedback = PlantFeedback(
                        feedbackId = UUID.randomUUID().toString(),
                        uid = uid,
                        gardenPlantId = gardenPlant.gardenPlantId,
                        originalScientificName = gardenPlant.scientificName,
                        originalConfidence = confidence,
                        isCorrect = false,
                        correctedCatalogPlantId = corrected.id,
                        correctedScientificName = corrected.scientificName,
                        createdAt = System.currentTimeMillis()
                    )
                    feedbackRepository.submitFeedback(feedback)
                }
                val updated = gardenRepository.getSavedPlant(corrected.id)
                _uiState.value = _uiState.value.copy(
                    plant = corrected,
                    gardenPlant = updated,
                    isSubmittingFeedback = false,
                    showCatalogPicker = false,
                    hasSubmittedFeedback = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSubmittingFeedback = false,
                    showCatalogPicker = false,
                    feedbackErrorMessage = result.exceptionOrNull()?.message ?: "Error al corregir"
                )
            }
        }
    }

    fun dismissCatalogPicker() {
        _uiState.value = _uiState.value.copy(showCatalogPicker = false)
    }

    fun onFeedbackErrorShown() {
        _uiState.value = _uiState.value.copy(feedbackErrorMessage = null)
    }

    fun onErrorShown() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}