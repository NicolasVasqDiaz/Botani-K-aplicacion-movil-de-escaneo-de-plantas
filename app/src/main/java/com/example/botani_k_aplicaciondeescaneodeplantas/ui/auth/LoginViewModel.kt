package com.example.botani_k_aplicaciondeescaneodeplantas.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.botani_k_aplicaciondeescaneodeplantas.domain.repository.AuthRepository
import com.example.botani_k_aplicaciondeescaneodeplantas.domain.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val authError: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, authError = null)
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, authError = result.message)
                }
            }
        }
    }

    fun signInAsGuest(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, authError = null)
            when (val result = authRepository.signInAsGuest()) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, authError = result.message)
                }
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(authError = null)
    }

    /** Errores que vienen de fuera del repositorio (p. ej. Credential Manager). */
    fun onExternalError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, authError = message)
    }
}