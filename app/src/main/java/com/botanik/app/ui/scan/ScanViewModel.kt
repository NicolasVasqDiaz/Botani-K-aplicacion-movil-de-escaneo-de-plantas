package com.botanik.app.ui.scan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botanik.app.data.remote.PlantIdentifier
import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.domain.model.CustomSpecies
import com.botanik.app.domain.model.PlantIdentification
import com.botanik.app.domain.repository.CatalogRepository
import com.botanik.app.domain.repository.CloudinaryRepository
import com.botanik.app.domain.repository.CustomSpeciesRepository
import com.botanik.app.domain.repository.GardenRepository
import com.botanik.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object ShowingSourceSheet : ScanUiState
    data object CapturingPhoto : ScanUiState
    data object PickingPhoto : ScanUiState
    data object Identifying : ScanUiState
    data class Identified(
        val identifications: List<PlantIdentification>,
        val photoUri: String,
        val cloudinaryUrl: String
    ) : ScanUiState
    data class Matched(
        val plantId: String,
        val photoUrl: String,
        val mlConfidence: Float
    ) : ScanUiState
    data class ShowingCatalogFallback(
        val scientificName: String,
        val cloudinaryUrl: String,
        val confidence: Float
    ) : ScanUiState
    data object SavingCustomSpecies : ScanUiState
    data object CustomSpeciesSaved : ScanUiState
    data object UploadingPhoto : ScanUiState
    data class PhotoReady(val photoUri: String) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val cloudinaryRepository: CloudinaryRepository,
    private val plantIdentifier: PlantIdentifier,
    private val catalogRepository: CatalogRepository,
    private val customSpeciesRepository: CustomSpeciesRepository,
    private val gardenRepository: GardenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val state = catalogRepository.getAll().first { it !is UiState.Loading }
            if (state is UiState.Success) {
                android.util.Log.d("BotaniK_Match", "Catálogo precargado: ${state.data.size} plantas")
            } else if (state is UiState.Error) {
                android.util.Log.e("BotaniK_Match", "Error precargando catálogo: ${state.message}")
            }
        }
    }

    fun onScanClick() {
        _uiState.value = ScanUiState.ShowingSourceSheet
    }

    fun onSourceSelected(isCamera: Boolean) {
        _uiState.value = if (isCamera) ScanUiState.CapturingPhoto else ScanUiState.PickingPhoto
    }

    fun onPhotoCaptured(uri: Uri) {
        identifyAndUpload(uri)
    }

    fun onPhotoPicked(uri: Uri?) {
        if (uri == null) {
            _uiState.value = ScanUiState.Idle
            return
        }
        identifyAndUpload(uri)
    }

    private fun identifyAndUpload(uri: Uri) {
        _uiState.value = ScanUiState.Identifying
        viewModelScope.launch {
            val identifications = plantIdentifier.identify(uri, topK = 3)
            val uriString = uri.toString()

            if (identifications == null || identifications.isEmpty()) {
                uploadToCloudinary(uri)
                return@launch
            }

            cloudinaryRepository.uploadImage(context, uri).fold(
                onSuccess = { url ->
                    _uiState.value = ScanUiState.Identified(
                        identifications = identifications,
                        photoUri = uriString,
                        cloudinaryUrl = url
                    )
                },
                onFailure = { e ->
                    _uiState.value = ScanUiState.Error(e.message ?: "No pudimos subir la foto")
                }
            )
        }
    }

    fun onIdentificationSelected(identification: PlantIdentification) {
        val currentState = _uiState.value as? ScanUiState.Identified ?: return
        viewModelScope.launch {
            val match = findCatalogMatch(identification.scientificName)
            _uiState.value = if (match != null) {
                ScanUiState.Matched(
                    plantId = match.id,
                    photoUrl = currentState.cloudinaryUrl,
                    mlConfidence = identification.confidence
                )
            } else {
                ScanUiState.ShowingCatalogFallback(
                    scientificName = identification.scientificName,
                    cloudinaryUrl = currentState.cloudinaryUrl,
                    confidence = identification.confidence
                )
            }
        }
    }

    private val labelToCatalogId = mapOf(
        "aliso" to "aliso",
        "amancaes" to "amancaes",
        "ascidiogyne" to "ascidiogyne-sanchezvegae",
        "begonia_bosque" to "begonia-bosque",
        "bomarea_peruviana" to "bomarea-peruviana",
        "browallia_guzmango" to "browallia-guzmangoa",
        "cactacea_cajamarca" to "cactacea-cajamarca",
        "calceolaria_caespitosa" to "calceolaria-caespitosa",
        "calceolaria_cumbemayo" to "calceolaria-cumbemayensis",
        "canamuela" to "jaltomata-cajamarca",
        "cantuta" to "cantuta",
        "cascapilla" to "symplocos-sandemanii",
        "castilleja_isidoroi" to "castilleja-isidoroi",
        "chachacomo" to "chachacomo",
        "chacpa" to "oreocallis-grandiflora",
        "chamaecrista" to "chamaecrista-glandulosa",
        "chuquiragua" to "chuquiragua",
        "dyssodia_jelskii" to "dyssodia-jelskii",
        "galactia_augusti" to "galactia-augusti",
        "guinda" to "guinda",
        "hierba_santa" to "hierba-santa",
        "intimpa" to "podocarpus-glomeratus",
        "matico" to "matico",
        "molle" to "molle",
        "muna" to "muna",
        "nasa_cajamarca" to "nasa-dillonii",
        "orquidea_cajamarca" to "orquidea-cajamarca",
        "pacra_pacra" to "laccopetalum-giganteum",
        "papa_andina" to "papa-andina-silvestre",
        "papa_chiquiden" to "solanum-chiquidenum",
        "poro_poro" to "poro-poro",
        "quinua" to "quinua",
        "quishuar" to "quishuar",
        "rauhia" to "rauhia-multiflora",
        "salvia_celendin" to "salvia-vargas-llosae",
        "sauco" to "sauco",
        "senecio_cajamarca" to "senecio-szyszylowiczii",
        "tarwi" to "tarwi",
        "valeriana_cumbemayo" to "valeriana-cumbemayensis",
        "zapote" to "zapote"
    )

    private suspend fun findCatalogMatch(identifiedName: String): CatalogPlant? {
        val state = catalogRepository.getAll().first { it !is UiState.Loading }
        val plants = (state as? UiState.Success)?.data ?: return null
        if (plants.isEmpty()) return null

        val label = identifiedName.trim().lowercase()
        val labelSpaced = label.replace("_", " ")

        labelToCatalogId[label]?.let { catalogId ->
            plants.firstOrNull { it.id == catalogId }?.let { return it }
        }

        val normalizedHyphen = label.replace("_", "-")
        plants.firstOrNull {
            it.id.trim().lowercase().replace("_", "-") == normalizedHyphen
        }?.let { return it }

        plants.firstOrNull {
            it.scientificName.trim().lowercase() == label
        }?.let { return it }

        plants.firstOrNull {
            it.commonName.trim().lowercase() == label
        }?.let { return it }

        plants.firstOrNull {
            it.commonName.trim().lowercase() == labelSpaced
        }?.let { return it }

        plants.firstOrNull {
            labelSpaced.length >= 4 && it.commonName.trim().lowercase().contains(labelSpaced)
        }?.let { return it }

        plants.firstOrNull {
            labelSpaced.length >= 4 && it.scientificName.trim().lowercase().contains(labelSpaced)
        }?.let { return it }

        return null
    }

    private fun uploadToCloudinary(uri: Uri) {
        _uiState.value = ScanUiState.UploadingPhoto
        viewModelScope.launch {
            cloudinaryRepository.uploadImage(context, uri).fold(
                onSuccess = { url ->
                    _uiState.value = ScanUiState.PhotoReady(url)
                },
                onFailure = { e ->
                    _uiState.value = ScanUiState.Error(e.message ?: "No pudimos subir la foto")
                }
            )
        }
    }

    fun saveAsCustomSpecies() {
        val currentState = _uiState.value
        if (currentState !is ScanUiState.ShowingCatalogFallback) return

        viewModelScope.launch {
            _uiState.value = ScanUiState.SavingCustomSpecies

            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                _uiState.value = ScanUiState.Error("Sesión no iniciada")
                return@launch
            }

            val species = CustomSpecies(
                speciesId = UUID.randomUUID().toString(),
                scientificName = currentState.scientificName,
                commonName = currentState.scientificName,
                family = "",
                region = "",
                floweringSeason = "",
                care = emptyMap(),
                funFacts = emptyList(),
                imageUrl = currentState.cloudinaryUrl,
                isEndemic = false,
                source = "ml_identification",
                confidence = currentState.confidence,
                createdBy = uid,
                createdAt = System.currentTimeMillis()
            )

            customSpeciesRepository.saveCustomSpecies(species).fold(
                onSuccess = {
                    gardenRepository.saveCustomPlant(
                        species = species,
                        userPhotoUrl = currentState.cloudinaryUrl,
                        mlConfidence = currentState.confidence
                    ).fold(
                        onSuccess = {
                            _uiState.value = ScanUiState.CustomSpeciesSaved
                        },
                        onFailure = { e ->
                            _uiState.value = ScanUiState.Error(e.message ?: "Error al guardar en Mi Jardín")
                        }
                    )
                },
                onFailure = { e ->
                    _uiState.value = ScanUiState.Error(e.message ?: "Error al guardar la especie")
                }
            )
        }
    }

    fun onDismiss() {
        _uiState.value = ScanUiState.Idle
    }

    fun onErrorShown() {
        _uiState.value = ScanUiState.Idle
    }
}