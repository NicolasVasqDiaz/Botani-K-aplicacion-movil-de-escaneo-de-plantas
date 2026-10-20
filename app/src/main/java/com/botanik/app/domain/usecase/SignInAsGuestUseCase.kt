package com.botanik.app.domain.usecase

import com.botanik.app.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Caso de uso para iniciar sesión de forma anónima (modo invitado / explorador escolar).
 * Permite acceder a las funciones del aplicativo sin necesidad de vincular una cuenta de Google.
 */
class SignInAsGuestUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.signInAsGuest()
    }
}
