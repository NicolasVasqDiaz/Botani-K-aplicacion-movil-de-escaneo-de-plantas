package com.botanik.app.data.repository

import com.botanik.app.data.remote.FirestoreUserDataSource
import com.botanik.app.domain.model.AuthState
import com.botanik.app.domain.repository.AuthRepository
import com.botanik.app.domain.repository.UserRepository
import com.botanik.app.domain.model.UserProfile
import com.botanik.app.ui.common.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreUserDataSource,
    private val authRepository: AuthRepository
) : UserRepository {

    override fun observeUser(uid: String): Flow<UiState<UserProfile>> =
        dataSource.observeUser(uid).map { profile ->
            if (profile != null) UiState.Success(profile) else UiState.Error("No encontramos tu perfil")
        }

    override suspend fun ensureUserExists(
        uid: String, email: String, displayName: String, photoUrl: String?
    ): Result<Unit> = runCatching {
        dataSource.ensureUserExists(uid, email, displayName, photoUrl)
    }

    override suspend fun updateDisplayName(name: String): Result<Unit> {
        val uid = currentUidOrNull() ?: return Result.failure(IllegalStateException("Sesión no iniciada"))
        return dataSource.updateDisplayName(uid, name)
    }

    override suspend fun updatePhotoUrl(url: String): Result<Unit> {
        val uid = currentUidOrNull() ?: return Result.failure(IllegalStateException("Sesión no iniciada"))
        return dataSource.updatePhotoUrl(uid, url)
    }

    override suspend fun removePhotoUrl(): Result<Unit> {
        val uid = currentUidOrNull() ?: return Result.failure(IllegalStateException("Sesión no iniciada"))
        return dataSource.removePhotoUrl(uid)
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean): Result<Unit> {
        val uid = currentUidOrNull() ?: return Result.failure(IllegalStateException("Sesión no iniciada"))
        return dataSource.updateNotificationsEnabled(uid, enabled)
    }

    private suspend fun currentUidOrNull(): String? =
        (authRepository.authState.first() as? AuthState.Authenticated)?.userId
}
