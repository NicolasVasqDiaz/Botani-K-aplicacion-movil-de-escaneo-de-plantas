package com.example.botani_k_aplicaciondeescaneodeplantas.data.repository

import com.example.botani_k_aplicaciondeescaneodeplantas.data.remote.FirebaseAuthDataSource
import com.example.botani_k_aplicaciondeescaneodeplantas.domain.repository.AuthRepository
import com.example.botani_k_aplicaciondeescaneodeplantas.domain.repository.AuthResult
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return authDataSource.signInWithGoogleCredential(idToken).fold(
            onSuccess = { (uid, name) -> AuthResult.Success(uid, name) },
            onFailure = { e -> AuthResult.Error(mapError(e, "Google")) }
        )
    }

    override suspend fun signInAsGuest(): AuthResult {
        return authDataSource.signInAnonymously().fold(
            onSuccess = { (uid, name) -> AuthResult.Success(uid, name) },
            onFailure = { e -> AuthResult.Error(mapError(e, "invitado")) }
        )
    }

    override suspend fun signOut() {
        authDataSource.signOut()
    }

    override fun isUserLoggedIn(): Boolean = authDataSource.currentUserId() != null

    private fun mapError(e: Throwable, method: String): String {
        val raw = e.message.orEmpty()
        return when {
            e.javaClass.simpleName.contains("Network", ignoreCase = true) ||
                raw.contains("network", ignoreCase = true) ||
                raw.contains("unreachable", ignoreCase = true) ->
                "Sin conexión a internet. Revisa tu red e inténtalo de nuevo."
            raw.contains("restricted to administrators", ignoreCase = true) ||
                raw.contains("ADMIN_ONLY", ignoreCase = true) ||
                raw.contains("admin-restricted-operation", ignoreCase = true) ->
                "El acceso como $method no está habilitado en Firebase Console. " +
                    "Actívalo en Authentication > Sign-in method."
            else -> "No se pudo iniciar sesión como $method: ${raw.ifEmpty { "error desconocido" }}"
        }
    }
}