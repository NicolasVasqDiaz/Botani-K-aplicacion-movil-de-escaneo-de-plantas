package com.botanik.app.data.remote

import com.botanik.app.domain.model.GardenPlant
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/** Fuente de datos de la subcolección `users/{uid}/garden`. */
class FirestoreGardenDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun gardenCollection(uid: String) =
        firestore.collection("users").document(uid).collection("garden")

    fun observeGarden(uid: String): Flow<List<GardenPlant>> = callbackFlow {
        val registration = gardenCollection(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val plants = snapshot?.documents?.mapNotNull { it.toGardenPlant() } ?: emptyList()
            trySend(plants)
        }
        awaitClose { registration.remove() }
    }

    suspend fun addPlant(uid: String, plant: GardenPlant): String {
        val docRef = gardenCollection(uid).document(plant.gardenPlantId)
        docRef.set(plant.toFirestoreMap()).await()
        return docRef.id
    }

    /** Actualiza una planta existente (usado al corregir feedback). */
    suspend fun updatePlant(uid: String, plant: GardenPlant) {
        gardenCollection(uid).document(plant.gardenPlantId)
            .set(plant.toFirestoreMap(), SetOptions.merge())
            .await()
    }

    suspend fun deletePlant(uid: String, gardenPlantId: String) {
        gardenCollection(uid).document(gardenPlantId).delete().await()
    }

    private fun GardenPlant.toFirestoreMap(): Map<String, Any?> = mapOf(
        "catalogPlantId" to catalogPlantId,
        "commonName" to commonName,
        "scientificName" to scientificName,
        "family" to family,
        "region" to region,
        "floweringSeason" to floweringSeason,
        "care" to care,
        "funFacts" to funFacts,
        "userPhotoUrl" to userPhotoUrl,
        "savedAt" to savedAt,
        "mlConfidence" to mlConfidence
    )

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toGardenPlant(): GardenPlant? {
        if (!exists()) return null
        return GardenPlant(
            gardenPlantId = id,
            catalogPlantId = getString("catalogPlantId") ?: return null,
            commonName = getString("commonName") ?: "",
            scientificName = getString("scientificName") ?: "",
            family = getString("family") ?: "",
            region = getString("region") ?: "",
            floweringSeason = getString("floweringSeason") ?: "",
            care = toCareMap(),
            funFacts = (get("funFacts") as? List<String>) ?: emptyList(),
            userPhotoUrl = getString("userPhotoUrl"),
            savedAt = getLong("savedAt") ?: 0L,
            mlConfidence = getDouble("mlConfidence")?.toFloat()
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toCareMap(): Map<String, String> {
        val raw = get("care") as? Map<*, *> ?: return emptyMap()
        return raw.mapNotNull { (k, v) ->
            val key = k as? String ?: return@mapNotNull null
            val value = v as? String ?: return@mapNotNull null
            key to value
        }.toMap()
    }
}