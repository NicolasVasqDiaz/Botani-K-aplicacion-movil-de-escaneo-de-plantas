package com.botanik.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.localDataStore: DataStore<Preferences> by preferencesDataStore(name = "local_session")

@Singleton
class LocalSessionManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val currentUserIdKey = stringPreferencesKey("current_user_id")
    private val currentFirebaseUidKey = stringPreferencesKey("current_firebase_uid")

    val currentUserId: Flow<String?> = context.localDataStore.data.map { prefs ->
        prefs[currentUserIdKey]
    }

    /**
     * UID de Firebase (anónimo) asociado a la sesión local actual.
     * Se guarda para que la cuenta local pueda leer y escribir en Firestore
     * bajo `users/{uid}` usando las mismas reglas de seguridad que Google.
     */
    val currentFirebaseUid: Flow<String?> = context.localDataStore.data.map { prefs ->
        prefs[currentFirebaseUidKey]
    }

    suspend fun saveSession(userId: String, firebaseUid: String? = null) {
        context.localDataStore.edit { prefs ->
            prefs[currentUserIdKey] = userId
            if (firebaseUid != null) {
                prefs[currentFirebaseUidKey] = firebaseUid
            }
        }
    }

    suspend fun clearSession() {
        context.localDataStore.edit { prefs ->
            prefs.remove(currentUserIdKey)
            prefs.remove(currentFirebaseUidKey)
        }
    }

    suspend fun getCurrentUserId(): String? = currentUserId.first()
}
