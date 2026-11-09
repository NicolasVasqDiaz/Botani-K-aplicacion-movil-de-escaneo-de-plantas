package com.botanik.app.data.remote

import com.botanik.app.domain.model.PlantFeedback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val USERS_COLLECTION = "users"
private const val FEEDBACK_COLLECTION = "feedback"

class FirestoreFeedbackDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun uid(): String =
        FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Usuario no autenticado")

    private fun feedbackCollection() =
        firestore.collection(USERS_COLLECTION).document(uid()).collection(FEEDBACK_COLLECTION)

    suspend fun add(feedback: PlantFeedback): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            feedbackCollection().document(feedback.feedbackId)
                .set(feedback.toFirestoreMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("No pudimos guardar tu feedback: ${e.message}", e))
        }
    }

    suspend fun getByGardenPlantId(gardenPlantId: String): Result<PlantFeedback?> =
        withContext(Dispatchers.IO) {
            try {
                val docs = feedbackCollection()
                    .whereEqualTo("gardenPlantId", gardenPlantId)
                    .limit(1)
                    .get()
                    .await()
                    .documents
                Result.success(docs.firstOrNull()?.toFeedback())
            } catch (e: Exception) {
                Result.failure(Exception("No pudimos leer el feedback: ${e.message}", e))
            }
        }

    private fun PlantFeedback.toFirestoreMap(): Map<String, Any?> = mapOf(
        "feedbackId" to feedbackId,
        "uid" to uid,
        "gardenPlantId" to gardenPlantId,
        "originalScientificName" to originalScientificName,
        "originalConfidence" to originalConfidence,
        "isCorrect" to isCorrect,
        "correctedCatalogPlantId" to correctedCatalogPlantId,
        "correctedScientificName" to correctedScientificName,
        "createdAt" to createdAt
    )

    private fun DocumentSnapshot.toFeedback(): PlantFeedback? {
        if (!exists()) return null
        return try {
            PlantFeedback(
                feedbackId = getString("feedbackId") ?: id,
                uid = getString("uid") ?: "",
                gardenPlantId = getString("gardenPlantId") ?: "",
                originalScientificName = getString("originalScientificName") ?: "",
                originalConfidence = getDouble("originalConfidence")?.toFloat() ?: 0f,
                isCorrect = getBoolean("isCorrect") ?: false,
                correctedCatalogPlantId = getString("correctedCatalogPlantId"),
                correctedScientificName = getString("correctedScientificName"),
                createdAt = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}