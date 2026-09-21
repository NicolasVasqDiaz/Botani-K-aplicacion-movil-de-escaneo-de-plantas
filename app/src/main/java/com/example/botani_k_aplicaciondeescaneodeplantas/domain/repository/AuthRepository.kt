package com.example.botani_k_aplicaciondeescaneodeplantas.domain.repository

sealed class AuthResult {
    data class Success(val userId: String, val displayName: String?) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun signInAsGuest(): AuthResult
    suspend fun signOut()
    fun isUserLoggedIn(): Boolean
}