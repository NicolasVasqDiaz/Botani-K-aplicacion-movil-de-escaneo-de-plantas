package com.botanik.app.data.remote

import com.botanik.app.domain.model.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun userDoc(uid: String) = firestore.collection("users").document(uid)

    fun observeUser(uid: String): Flow<UserProfile?> = callbackFlow {
        val registration = userDoc(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toUserProfile())
        }
        awaitClose { registration.remove() }
    }

    /**
     * Crea el documento `users/{uid}` solo si no existe todavía. Si ya existe,
     * no lo sobrescribe (evita pisar preferencias que el usuario ya cambió,
     * como notificationsEnabled, en cada login).
     */
    suspend fun ensureUserExists(uid: String, email: String, displayName: String, photoUrl: String?) {
        val doc = userDoc(uid).get().await()
        if (doc.exists()) return

        userDoc(uid).set(
            mapOf(
                "uid" to uid,
                "email" to email,
                "displayName" to displayName,
                "photoUrl" to photoUrl,
                "notificationsEnabled" to true,
                "createdAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun updateDisplayName(uid: String, name: String): Result<Unit> = runCatching {
        userDoc(uid).update("displayName", name).await()
    }

    suspend fun updatePhotoUrl(uid: String, url: String): Result<Unit> = runCatching {
        userDoc(uid).update("photoUrl", url).await()
    }

    /**
     * Elimina la foto de perfil borrando el campo `photoUrl` del documento.
     * Se usa `deleteField` en lugar de escribir `null` porque Firestore
     * guarda `null` como un valor y `getString("photoUrl")` lo devolvería
     * como texto vacío en lugar de ausente.
     */
    suspend fun removePhotoUrl(uid: String): Result<Unit> = runCatching {
        userDoc(uid).update("photoUrl", FieldValue.delete()).await()
    }

    suspend fun updateNotificationsEnabled(uid: String, enabled: Boolean): Result<Unit> = runCatching {
        userDoc(uid).update("notificationsEnabled", enabled).await()
    }

    private fun DocumentSnapshot.toUserProfile(): UserProfile? {
        if (!exists()) return null
        return UserProfile(
            uid = id,
            displayName = getString("displayName") ?: "",
            email = getString("email") ?: "",
            photoUrl = getString("photoUrl"),
            ageRange = getString("ageRange"),
            userType = getString("userType"),
            notificationsEnabled = getBoolean("notificationsEnabled") ?: true,
            createdAt = getLong("createdAt") ?: 0L
        )
    }
}
