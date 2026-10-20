package com.botanik.app.domain.repository

import com.botanik.app.domain.model.UserProfile
import com.botanik.app.ui.common.UiState
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUser(uid: String): Flow<UiState<UserProfile>>
    suspend fun ensureUserExists(uid: String, email: String, displayName: String, photoUrl: String?): Result<Unit>
    suspend fun updateDisplayName(name: String): Result<Unit>
    suspend fun updatePhotoUrl(url: String): Result<Unit>
    suspend fun removePhotoUrl(): Result<Unit>
    suspend fun updateNotificationsEnabled(enabled: Boolean): Result<Unit>
}
