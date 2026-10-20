package com.botanik.app.domain.repository

import com.botanik.app.data.local.LocalUserEntity
import com.botanik.app.domain.model.AuthState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun signInAsGuest(): Result<Unit>
    suspend fun signOut(): Result<Unit>
    suspend fun registerLocalUser(name: String, email: String, password: String): Result<Unit>
    suspend fun signInWithEmail(email: String, password: String): Result<Unit>
    suspend fun getLocalUser(): LocalUserEntity?
    suspend fun signOutLocal(): Result<Unit>
}
