package com.botanik.app.ui.profile

import android.content.Context
import android.net.Uri
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botanik.app.domain.model.AuthState
import com.botanik.app.domain.repository.AuthRepository
import com.botanik.app.domain.repository.CloudinaryRepository
import com.botanik.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val error: String? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val photoUrl: String? = null,
    val notificationsEnabled: Boolean = true,
    val isEditingName: Boolean = false,
    val nameDraft: String = "",
    val showEditNameDialog: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null
)

/**
 * ViewModel de la pantalla de perfil.
 *
 * Qué:    expone los datos del usuario y permite editarlos.
 * Cómo:   la sesión (nombre/email) viene del flujo unificado de autenticación;
 *         la foto y las notificaciones se leen del documento `users/{uid}`
 *         en Firestore mediante [UserRepository.observeUser].
 * Por qué: el nombre se mantiene en la fuente de autenticación (para que
 *         las cuentas de Google reflejen el cambio) mientras que la foto y
 *         las notificaciones viven en el perfil de Firestore.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val cloudinaryRepository: CloudinaryRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // El flujo de auth es unificado (Google + cuenta local), así que aquí
            // llega el nombre tanto para usuarios de Google como para cuentas
            // creadas dentro de la app.
            authRepository.authState.collect { state ->
                if (state is AuthState.Authenticated) {
                    _uiState.value = _uiState.value.copy(
                        userName = state.displayName,
                        userEmail = state.email,
                        photoUrl = state.photoUrl ?: _uiState.value.photoUrl
                    )
                    observeUserProfile(state.userId)
                }
            }
        }
    }

    /**
     * Observa el documento `users/{uid}` en Firestore para mantener la foto
     * de perfil y la preferencia de notificaciones sincronizadas.
     */
    private fun observeUserProfile(uid: String) {
        viewModelScope.launch {
            userRepository.observeUser(uid).collect { state ->
                if (state is com.botanik.app.ui.common.UiState.Success) {
                    _uiState.value = _uiState.value.copy(
                        photoUrl = state.data.photoUrl ?: _uiState.value.photoUrl,
                        notificationsEnabled = state.data.notificationsEnabled
                    )
                }
            }
        }
    }

    // ── Edición de nombre ──────────────────────────────────────

    /** Abre el diálogo de edición cargando el nombre actual como borrador. */
    fun onEditNameClick() {
        _uiState.value = _uiState.value.copy(
            showEditNameDialog = true,
            nameDraft = _uiState.value.userName.orEmpty(),
            error = null
        )
    }

    fun onNameDraftChange(value: String) {
        _uiState.value = _uiState.value.copy(nameDraft = value)
    }

    fun onDismissEditName() {
        _uiState.value = _uiState.value.copy(showEditNameDialog = false, nameDraft = "")
    }

    /** Guarda el nuevo nombre del usuario en Firestore. */
    fun onSaveName() {
        val newName = _uiState.value.nameDraft.trim()
        if (newName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "El nombre no puede estar vacío")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            userRepository.updateDisplayName(newName)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        showEditNameDialog = false,
                        nameDraft = "",
                        userName = newName,
                        message = "Nombre actualizado"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = e.message ?: "No pudimos actualizar tu nombre"
                    )
                }
        }
    }

    // ── Foto de perfil ─────────────────────────────────────────

    /**
     * Guarda la URL de la foto de perfil.
     *
     * @param url URL de la imagen (por ejemplo, la de Cloudinary).
     */
    fun onSavePhotoUrl(url: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            userRepository.updatePhotoUrl(url)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        photoUrl = url,
                        message = "Foto actualizada"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = e.message ?: "No pudimos actualizar tu foto"
                    )
                }
        }
    }

    /** Elimina la foto de perfil (borra el campo `photoUrl` del documento). */
    fun onRemovePhoto() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            userRepository.removePhotoUrl()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        photoUrl = null,
                        message = "Foto eliminada"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = e.message ?: "No pudimos eliminar tu foto"
                    )
                }
        }
    }

    /**
     * Sube la imagen elegida a Cloudinary y guarda la URL resultante en el
     * perfil del usuario.
     *
     * Qué:    reemplaza la foto de perfil.
     * Cómo:   la imagen local se sube a Cloudinary (mismo mecanismo que las
     *         fotos de plantas) y la URL devuelta se persiste en
     *         `users/{uid}.photoUrl`.
     * Por qué: una URI local (`content://...`) solo vive en el dispositivo;
     *         guardarla en Firestore la dejaría rota para el resto de
     *         dispositivos y al volver a abrir la app.
     */
    fun onPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            cloudinaryRepository.uploadImage(context, uri)
                .onSuccess { url ->
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    onSavePhotoUrl(url)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = e.message ?: "No pudimos subir tu foto. Intenta de nuevo"
                    )
                }
        }
    }

    // ── Notificaciones ──────────────────────────────────────────

    /** Activa o desactiva las notificaciones del usuario. */
    fun onNotificationsChange(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(notificationsEnabled = enabled, error = null)
            userRepository.updateNotificationsEnabled(enabled)
                .onFailure { e ->
                    // Revierte el valor visual si el guardado falló.
                    _uiState.value = _uiState.value.copy(
                        notificationsEnabled = !enabled,
                        error = e.message ?: "No pudimos guardar la preferencia"
                    )
                }
        }
    }

    // ── Sesión y mensajes ───────────────────────────────────────

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            // `signOut` ya limpia la sesión local (Room + DataStore) y la de
            // Firebase, por lo que no hace falta llamar a `signOutLocal`.
            authRepository.signOut()
                .onSuccess {
                    // Limpia las credenciales guardadas para que Google no
                    // vuelva a autenticar en silencio al regresar al Login.
                    try {
                        CredentialManager.create(context)
                            .clearCredentialState(ClearCredentialStateRequest())
                    } catch (_: Exception) {
                        // No fatal: la sesión de Firebase ya se cerró.
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoggedOut = true)
                    onDone()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "No se pudo cerrar sesión"
                    )
                }
        }
    }

    fun onErrorShown() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun onMessageShown() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
