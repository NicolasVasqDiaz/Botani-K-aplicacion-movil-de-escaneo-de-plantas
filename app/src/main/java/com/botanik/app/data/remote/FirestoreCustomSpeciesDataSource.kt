package com.botanik.app.data.remote

import com.botanik.app.domain.model.CustomSpecies
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val USERS_COLLECTION = "users"
private const val CUSTOM_CATALOG = "custom_catalog"

class FirestoreCustomSpeciesDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun uid(): String =
        FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Usuario no autenticado")

    suspend fun add(species: CustomSpecies): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = uid()
            firestore.collection(USERS_COLLECTION).document(uid)
                .collection(CUSTOM_CATALOG).document(species.speciesId)
                .set(species.toFirestoreMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("No pudimos guardar la especie: ${e.message}", e))
        }
    }

    suspend fun getById(speciesId: String): Result<CustomSpecies?> = withContext(Dispatchers.IO) {
        try {
            val uid = uid()
            val doc = firestore.collection(USERS_COLLECTION).document(uid)
                .collection(CUSTOM_CATALOG).document(speciesId)
                .get().await()
            Result.success(doc.toCustomSpecies())
        } catch (e: Exception) {
            Result.failure(Exception("No pudimos leer la especie: ${e.message}", e))
        }
    }

    suspend fun getAll(): Result<List<CustomSpecies>> = withContext(Dispatchers.IO) {
        try {
            val uid = uid()
            val docs = firestore.collection(USERS_COLLECTION).document(uid)
                .collection(CUSTOM_CATALOG).get().await().documents
            Result.success(docs.mapNotNull { it.toCustomSpecies() })
        } catch (e: Exception) {
            Result.failure(Exception("No pudimos leer el catálogo personal: ${e.message}", e))
        }
    }

    private fun CustomSpecies.toFirestoreMap(): Map<String, Any?> = mapOf(
        "speciesId" to speciesId,
        "scientificName" to scientificName,
        "commonName" to commonName,
        "family" to family,
        "region" to region,
        "floweringSeason" to floweringSeason,
        "care" to care,
        "funFacts" to funFacts,
        "imageUrl" to imageUrl,
        "isEndemic" to isEndemic,
        "source" to source,
        "confidence" to confidence,
        "createdBy" to createdBy,
        "createdAt" to createdAt
    )

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toCustomSpecies(): CustomSpecies? {
        if (!exists()) return null
        return try {
            CustomSpecies(
                speciesId = getString("speciesId") ?: id,
                scientificName = getString("scientificName") ?: "",
                commonName = getString("commonName") ?: "",
                family = getString("family") ?: "",
                region = getString("region") ?: "",
                floweringSeason = getString("floweringSeason") ?: "",
                care = (get("care") as? Map<*, *>)?.mapNotNull { (k, v) ->
                    val key = k as? String ?: return@mapNotNull null
                    val value = v as? String ?: return@mapNotNull null
                    key to value
                }?.toMap() ?: emptyMap(),
                funFacts = (get("funFacts") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                imageUrl = getString("imageUrl") ?: "",
                isEndemic = getBoolean("isEndemic") ?: false,
                source = getString("source") ?: "ml_identification",
                confidence = getDouble("confidence")?.toFloat() ?: 0f,
                createdBy = getString("createdBy") ?: "",
                createdAt = getLong("createdAt") ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}