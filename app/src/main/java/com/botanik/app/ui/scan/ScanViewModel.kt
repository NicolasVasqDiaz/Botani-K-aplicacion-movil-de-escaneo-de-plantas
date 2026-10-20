package com.botanik.app.ui.scan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    data object UploadingPhoto : ScanUiState
    data class PhotoReady(val photoUri: String) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val cloudinaryRepository: CloudinaryRepository
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
        uploadToCloudinary(uri)
    }

    fun onPhotoPicked(uri: Uri?) {
        if (uri == null) {
            _uiState.value = ScanUiState.Idle
            return
        }
        uploadToCloudinary(uri)
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

    fun onDismiss() {
        _uiState.value = ScanUiState.Idle
    }

    fun onErrorShown() {
        _uiState.value = ScanUiState.Idle
    }
}
