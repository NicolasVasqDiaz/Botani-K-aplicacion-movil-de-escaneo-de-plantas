package com.botanik.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Fuente de datos cruda para Firebase Auth. No contiene lógica de negocio
 * ni mapeo de errores a español — eso vive en [com.botanik.app.data.repository.AuthRepositoryImpl].
 */
class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    /** Autentica con Firebase usando el idToken obtenido de Google (Credential Manager). */
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(IllegalStateException("Usuario nulo tras autenticación"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Autentica de forma anónima con Firebase (modo invitado / explorador escolar). */
    suspend fun signInAsGuest(): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInAnonymously().await()
            val user = result.user ?: return Result.failure(IllegalStateException("Usuario nulo tras autenticación anónima"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Cierra la sesión actual de Firebase. */
    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Usuario actualmente autenticado, o null si no hay sesión. */
    fun currentUser(): FirebaseUser? = firebaseAuth.currentUser

    /**
     * Flow que emite el usuario autenticado cada vez que cambia el estado
     * de Firebase Auth (login, logout, restauración de sesión al abrir la app).
     */
    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }
}
