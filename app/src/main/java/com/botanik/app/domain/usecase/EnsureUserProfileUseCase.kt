package com.botanik.app.domain.usecase

import com.botanik.app.domain.model.AuthState
import com.botanik.app.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Crea el documento `users/{uid}` tras un login exitoso, si no existe aún.
 * Se invoca desde el observador de authState en la UI (ver notas abajo),
 * no desde AuthRepository, para evitar dependencia circular
 * AuthRepository → UserRepository.
 */
class EnsureUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(authenticated: AuthState.Authenticated) {
        userRepository.ensureUserExists(
            uid = authenticated.userId,
            email = authenticated.email ?: "",
            displayName = authenticated.displayName ?: "Explorador Botani-k",
            photoUrl = authenticated.photoUrl
        )
        // No se propaga el error: si falla, solo se loggea.
    }
}
