package com.botanik.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botanik.app.di.DatabaseWarmup
import com.botanik.app.domain.model.AuthState
import com.botanik.app.domain.repository.AuthRepository
import com.botanik.app.domain.usecase.EnsureUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Expone la sesión de Firebase al NavHost y garantiza que el perfil de usuario
 * exista en Firestore tras cada login (incluso con sesión persistente).
 * También verifica la sesión local (Room + DataStore) cuando no hay usuario de Firebase.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val ensureUserProfileUseCase: EnsureUserProfileUseCase,
    private val databaseWarmup: DatabaseWarmup
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // El estado unificado (Google + sesión local) llega de un solo flujo,
        // por lo que ya no hay dos coroutines compitiendo por escribirlo.
        viewModelScope.launch {
            authRepository.authState.collect { state ->
                _authState.value = state
                if (state is AuthState.Authenticated) {
                    runCatching { ensureUserProfileUseCase(state) }
                        .onFailure { /* No bloquea la navegación */ }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
