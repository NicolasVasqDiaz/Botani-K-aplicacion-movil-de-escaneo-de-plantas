package com.example.botani_k_aplicaciondeescaneodeplantas.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun signInWithGoogleCredential(idToken: String): Result<Pair<String, String?>> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user ?: return Result.failure(IllegalStateException("Usuario nulo tras autenticación"))
            Result.success(user.uid to user.displayName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInAnonymously(): Result<Pair<String, String?>> {
        return try {
            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user ?: return Result.failure(IllegalStateException("Usuario nulo tras autenticación anónima"))
            Result.success(user.uid to null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() = firebaseAuth.signOut()

    fun currentUserId(): String? = firebaseAuth.currentUser?.uid
}