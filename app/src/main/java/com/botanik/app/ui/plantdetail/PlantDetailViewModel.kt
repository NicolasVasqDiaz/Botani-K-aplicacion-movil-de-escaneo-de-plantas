package com.botanik.app.ui.plantdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.domain.model.PlantFeedback
import com.botanik.app.domain.repository.CatalogRepository
import com.botanik.app.domain.repository.FeedbackRepository
import com.botanik.app.domain.repository.GardenRepository
import com.botanik.app.domain.repository.ReminderRepository
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
    private val feedbackRepository: FeedbackRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlantDetailUiState())
    val uiState: StateFlow<PlantDetailUiState> = _uiState.asStateFlow()

    private var gardenJob: Job? = null

    fun loadPlantById(plantId: String, userPhotoUrl: String?, mlConfidence: Float? = null) {
        _uiState.value = _uiState.value.copy(isLoading = true, userPhotoUrl = userPhotoUrl, mlConfidence = mlConfidence)
        viewModelScope.launch {
            catalogRepository.getById(plantId).collect { state ->
                when (state) {
                    is UiState.Success -> {
                        _uiState.value = _uiState.value.copy(plant = state.data, isLoading = false, errorMessage = null)
                        checkIfInGarden(state.data.id)
                    }
                    is UiState.Error -> loadFromLocalCache(plantId)
                    else -> { }
                }
            }
        }
    }

    private suspend fun loadFromLocalCache(id: String) {
        var cached = gardenRepository.getSavedPlant(id)
        if (cached == null) cached = gardenRepository.getGardenPlantById(id)

        if (cached != null) {
            _uiState.value = _uiState.value.copy(
                plant = cached.toCatalogPlant(),
                isLoading = false,
                errorMessage = null,
                isFromCache = true,
                gardenPlant = cached,
                isSavedToGarden = true
            )
            checkIfInGarden(cached.catalogPlantId)
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "No pudimos cargar la planta.")
        }
    }

    private fun com.botanik.app.domain.model.GardenPlant.toCatalogPlant(): CatalogPlant =
        CatalogPlant(
            id = catalogPlantId, commonName = commonName, scientificName = scientificName,
            family = family, region = region, floweringSeason = floweringSeason,
            care = care, funFacts = funFacts, imageUrl = userPhotoUrl.orEmpty(), isEndemic = false
        )

    private fun checkIfInGarden(catalogPlantId: String) {
        gardenJob?.cancel()
        gardenJob = viewModelScope.launch {
            gardenRepository.isPlantInGarden(catalogPlantId).collect { isSaved ->
                _uiState.value = _uiState.value.copy(isSavedToGarden = isSaved)

                if (isSaved) {
                    val gardenPlant = gardenRepository.getSavedPlant(catalogPlantId)
                    val notes = if (gardenPlant != null) gardenRepository.getNotesForPlant(gardenPlant.gardenPlantId) else emptyList()

                    _uiState.value = _uiState.value.copy(gardenPlant = gardenPlant, notes = notes)

                    if (gardenPlant != null) {
                        feedbackRepository.getFeedbackForPlant(gardenPlant.gardenPlantId).fold(
                            onSuccess = { existing -> if (existing != null) _uiState.value = _uiState.value.copy(hasSubmittedFeedback = true) },
                            onFailure = { }
                        )
                    }
                }
            }
        }
    }

    fun toggleCareExpanded() { _uiState.value = _uiState.value.copy(isCareExpanded = !_uiState.value.isCareExpanded) }
    fun toggleCultureExpanded() { _uiState.value = _uiState.value.copy(isCultureExpanded = !_uiState.value.isCultureExpanded) }

    fun saveToGarden() {
        val plant = _uiState.value.plant ?: return
        viewModelScope.launch {
            gardenRepository.savePlant(plant, _uiState.value.userPhotoUrl, _uiState.value.mlConfidence).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSavedToGarden = true, errorMessage = null)
                    checkIfInGarden(plant.id)
                },
                onFailure = { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
            )
        }
    }

    fun addNote(text: String) {
        val gardenPlant = _uiState.value.gardenPlant ?: return
        viewModelScope.launch {
            val note = com.botanik.app.domain.model.PlantNote(
                noteId = UUID.randomUUID().toString(),
                gardenPlantId = gardenPlant.gardenPlantId,
                text = text,
                createdAt = System.currentTimeMillis()
            )
            gardenRepository.addNote(note).fold(
                onSuccess = {
                    val updatedNotes = gardenRepository.getNotesForPlant(gardenPlant.gardenPlantId)
                    _uiState.value = _uiState.value.copy(notes = updatedNotes)
                },
                onFailure = { }
            )
        }
    }

    // ═══ FEEDBACK Y RECORDATORIOS (Sin cambios) ═══
    fun submitPositiveFeedback() {
        val gardenPlant = _uiState.value.gardenPlant ?: return
        val confidence = gardenPlant.mlConfidence ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingFeedback = true)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val feedback = PlantFeedback(UUID.randomUUID().toString(), uid, gardenPlant.gardenPlantId, gardenPlant.scientificName, confidence, true, null, null, System.currentTimeMillis())
            feedbackRepository.submitFeedback(feedback).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isSubmittingFeedback = false, hasSubmittedFeedback = true) },
                onFailure = { e -> _uiState.value = _uiState.value.copy(isSubmittingFeedback = false, feedbackErrorMessage = e.message) }
            )
        }
    }

    fun onNegativeFeedbackClick() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingFeedback = true)
            val state = catalogRepository.getAll().first { it !is UiState.Loading }
            val plants = (state as? UiState.Success)?.data ?: emptyList()
            _uiState.value = _uiState.value.copy(showCatalogPicker = true, catalogForPicker = plants, isSubmittingFeedback = false)
        }
    }

    fun onCorrectSpeciesSelected(corrected: CatalogPlant) {
        val gardenPlant = _uiState.value.gardenPlant ?: return
        val confidence = gardenPlant.mlConfidence ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingFeedback = true)
            val result = gardenRepository.correctGardenPlant(gardenPlant.gardenPlantId, corrected)
            if (result.isSuccess) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val feedback = PlantFeedback(UUID.randomUUID().toString(), uid, gardenPlant.gardenPlantId, gardenPlant.scientificName, confidence, false, corrected.id, corrected.scientificName, System.currentTimeMillis())
                    feedbackRepository.submitFeedback(feedback)
                }
                val updated = gardenRepository.getSavedPlant(corrected.id)
                _uiState.value = _uiState.value.copy(plant = corrected, gardenPlant = updated, isSubmittingFeedback = false, showCatalogPicker = false, hasSubmittedFeedback = true)
            } else {
                _uiState.value = _uiState.value.copy(isSubmittingFeedback = false, showCatalogPicker = false, feedbackErrorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun dismissCatalogPicker() { _uiState.value = _uiState.value.copy(showCatalogPicker = false) }
    fun onFeedbackErrorShown() { _uiState.value = _uiState.value.copy(feedbackErrorMessage = null) }

    fun toggleReminder() {
        val gardenPlant = _uiState.value.gardenPlant ?: return
        updateReminder(!gardenPlant.reminderEnabled, gardenPlant.reminderFrequencyDays)
    }

    fun setReminderFrequency(days: Int) {
        val gardenPlant = _uiState.value.gardenPlant ?: return
        updateReminder(gardenPlant.reminderEnabled, days)
    }

    private fun updateReminder(enabled: Boolean, frequencyDays: Int) {
        val gardenPlant = _uiState.value.gardenPlant ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingReminder = true)
            val result = gardenRepository.updateReminder(gardenPlant.gardenPlantId, enabled, frequencyDays)
            if (result.isSuccess) {
                val updatedPlant = gardenPlant.copy(reminderEnabled = enabled, reminderFrequencyDays = frequencyDays)
                _uiState.value = _uiState.value.copy(gardenPlant = updatedPlant, isUpdatingReminder = false)
                if (enabled) reminderRepository.scheduleReminder(updatedPlant) else reminderRepository.cancelReminder(updatedPlant.gardenPlantId)
            } else {
                _uiState.value = _uiState.value.copy(isUpdatingReminder = false, reminderErrorMessage = result.exceptionOrNull()?.message)
            }
        }
    }

    fun onReminderErrorShown() { _uiState.value = _uiState.value.copy(reminderErrorMessage = null) }
    fun onErrorShown() { _uiState.value = _uiState.value.copy(errorMessage = null) }
}