package com.botanik.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botanik.app.domain.model.AuthState
import com.botanik.app.domain.repository.AuthRepository
import com.botanik.app.domain.usecase.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authState.collect { state ->
                _uiState.value = when (state) {
                    is AuthState.Authenticated -> _uiState.value.copy(isLoading = false, isAuthenticated = true, errorMessage = null)
                    is AuthState.Error -> _uiState.value.copy(isLoading = false, errorMessage = state.message)
                    is AuthState.Loading -> _uiState.value.copy(isLoading = true)
                    is AuthState.Unauthenticated -> _uiState.value.copy(isLoading = false, isAuthenticated = false)
                }
            }
        }
    }

    fun onSignInClick(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            signInWithGoogleUseCase(idToken).onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "No pudimos iniciar sesión. Intenta de nuevo")
            }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.signInWithEmail(email, password)
                .onSuccess {
                    // AuthViewModel will pick up the local session
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "No pudimos iniciar sesión"
                    )
                }
        }
    }

    fun onErrorShown() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
