package com.botanik.app.data.remote

import com.botanik.app.domain.model.CatalogPlant
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val CATALOG_COLLECTION = "catalog"

/**
 * Fuente de datos cruda para el catálogo botánico. El catálogo es estático
 * (seedeado una vez, solo lectura para usuarios), por eso usa lecturas
 * puntuales (get()) en lugar de addSnapshotListener.
 */
class FirestoreCatalogDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun fetchAll(): List<CatalogPlant> = withContext(Dispatchers.IO) {
        firestore.collection(CATALOG_COLLECTION)
            .get()
            .await()
            .documents
            .mapNotNull { it.toCatalogPlant() }
    }

    suspend fun fetchById(id: String): CatalogPlant? = withContext(Dispatchers.IO) {
        firestore.collection(CATALOG_COLLECTION)
            .document(id)
            .get()
            .await()
            .toCatalogPlant()
    }

    private fun DocumentSnapshot.toCatalogPlant(): CatalogPlant? {
        if (!exists()) return null
        return CatalogPlant(
            id = id,
            commonName = getString("commonName") ?: return null,
            scientificName = getString("scientificName") ?: "",
            family = getString("family") ?: "",
            region = getString("region") ?: "",
            floweringSeason = getString("floweringSeason") ?: "",
            care = toCareMap(),
            funFacts = toFunFactsList(),
            imageUrl = getString("imageUrl") ?: "",
            isEndemic = getBoolean("isEndemic") ?: false
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

    /** Convierte el campo `funFacts` de Firestore a una lista de cadenas segura. */
    private fun DocumentSnapshot.toFunFactsList(): List<String> {
        val raw = get("funFacts") as? List<*> ?: return emptyList()
        return raw.filterIsInstance<String>()
    }
}
