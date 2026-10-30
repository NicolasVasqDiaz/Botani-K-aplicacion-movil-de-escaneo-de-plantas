package com.botanik.app.ui.scan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botanik.app.data.remote.PlantIdentifier
import com.botanik.app.domain.repository.CloudinaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object ShowingSourceSheet : ScanUiState
    data object CapturingPhoto : ScanUiState
    data object PickingPhoto : ScanUiState
    data object Identifying : ScanUiState
    data class Identified(val speciesName: String, val photoUri: String) : ScanUiState
    data object UploadingPhoto : ScanUiState
    data class PhotoReady(val photoUri: String) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val cloudinaryRepository: CloudinaryRepository,
    private val plantIdentifier: PlantIdentifier
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

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
            // 1. Intentar identificar la planta localmente (on-device)
            val speciesName = plantIdentifier.identify(uri)
            val uriString = uri.toString()
            if (speciesName != null) {
                _uiState.value = ScanUiState.Identified(speciesName, uriString)
            } else {
                // Si no se pudo identificar, subir a Cloudinary y seguir el flujo normal
                uploadToCloudinary(uri)
            }
        }
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

    fun proceedAfterIdentification(uriString: String) {
        uploadToCloudinary(Uri.parse(uriString))
    }

    fun onDismiss() {
        _uiState.value = ScanUiState.Idle
    }

    fun onErrorShown() {
        _uiState.value = ScanUiState.Idle
    }
}
