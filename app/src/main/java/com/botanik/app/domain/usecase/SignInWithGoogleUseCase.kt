package com.botanik.app.domain.usecase

import com.botanik.app.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para iniciar sesión con Google. Valida el idToken antes de
 * delegar al repositorio para evitar llamadas innecesarias a Firebase.
 */
class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<Unit> {
        if (idToken.isBlank()) {
            return Result.failure(IllegalArgumentException("No pudimos obtener tus credenciales de Google. Intenta de nuevo"))
        }
        return authRepository.signInWithGoogle(idToken)
    }
}
