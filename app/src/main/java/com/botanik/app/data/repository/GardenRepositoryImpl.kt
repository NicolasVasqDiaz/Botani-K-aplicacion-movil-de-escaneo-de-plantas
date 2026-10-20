package com.botanik.app.data.repository

import com.botanik.app.data.local.GardenDao
import com.botanik.app.data.mapper.GardenMapper
import com.botanik.app.data.remote.FirestoreGardenDataSource
import com.botanik.app.domain.model.AuthState
import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.domain.model.GardenPlant
import com.botanik.app.domain.repository.AuthRepository
import com.botanik.app.domain.repository.GardenRepository
import com.botanik.app.ui.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

/**
 * Offline-first: Room es la fuente de verdad inmediata. Firestore sincroniza
 * en segundo plano; si Firestore falla, se mantiene lo último emitido desde
 * Room en lugar de propagar el error y vaciar la pantalla.
 */
class GardenRepositoryImpl @Inject constructor(
    private val gardenDao: GardenDao,
    private val firestoreDataSource: FirestoreGardenDataSource,
    private val authRepository: AuthRepository,
    private val mapper: GardenMapper
) : GardenRepository {

    override fun getGarden(): Flow<UiState<List<GardenPlant>>> = flow {
        emit(UiState.Loading)

        // 1. Emite Room primero (rápido, offline-first).
        val roomPlants = gardenDao.observeAll().first().map { with(mapper) { it.toDomain() } }
        emit(if (roomPlants.isEmpty()) UiState.Empty else UiState.Success(roomPlants))

        // 2. Luego sincroniza con Firestore en tiempo real.
        val uid = currentUidOrNull() ?: return@flow
        firestoreDataSource.observeGarden(uid)
            .catch {
                // Error de Firestore: mantener lo último de Room, no propagar error.
            }
            .collect { firestorePlants ->
                // Se combinan ambas fuentes en lugar de reemplazar: una emission
                // vacía de Firestore no debe borrar lo que ya está en Room.
                // Room manda en caso de conflicto (mismo gardenPlantId).
                val merged = mergeGardens(roomPlants, firestorePlants)
                emit(if (merged.isEmpty()) UiState.Empty else UiState.Success(merged))
            }
    }

    /**
     * Combina el jardín local (Room) con el de Firestore.
     *
     * Qué:    una sola lista con las plantas de ambas fuentes.
     * Cómo:   se indexa el jardín de Firestore por `gardenPlantId` y se
     *         recorren las plantas de Room; si la planta no está en Room se
     *         agrega la de Firestore (p. ej. guardada desde otro dispositivo).
     * Por qué: con cuentas locales el UID anónimo puede cambiar entre
     *         sesiones, dejando el jardín de Firestore en otra clave. Sin
     *         este merge, una respuesta vacía de Firestore vaciaría la
     *         pantalla aunque las plantasFollowing existieran en Room.
     */
    private fun mergeGardens(
        roomPlants: List<GardenPlant>,
        firestorePlants: List<GardenPlant>
    ): List<GardenPlant> {
        if (firestorePlants.isEmpty()) return roomPlants
        if (roomPlants.isEmpty()) return firestorePlants

        val roomIds = roomPlants.map { it.gardenPlantId }.toSet()
        val onlyInFirestore = firestorePlants.filterNot { it.gardenPlantId in roomIds }
        return (roomPlants + onlyInFirestore).sortedByDescending { it.savedAt }
    }

    override fun isPlantInGarden(catalogPlantId: String): Flow<Boolean> =
        gardenDao.isPlantInGarden(catalogPlantId)

    override suspend fun savePlant(catalogPlant: CatalogPlant, userPhotoUrl: String?): Result<Unit> {
        val uid = currentUidOrNull() ?: return Result.failure(IllegalStateException("Sesión no iniciada"))

        val plant = GardenPlant(
            gardenPlantId = UUID.randomUUID().toString(),
            catalogPlantId = catalogPlant.id,
            commonName = catalogPlant.commonName,
            scientificName = catalogPlant.scientificName,
            family = catalogPlant.family,
            region = catalogPlant.region,
            floweringSeason = catalogPlant.floweringSeason,
            care = catalogPlant.care,
            funFacts = catalogPlant.funFacts,
            userPhotoUrl = userPhotoUrl ?: catalogPlant.imageUrl.ifBlank { null },
            savedAt = System.currentTimeMillis()
        )

        // Optimista: Room primero.
        gardenDao.upsert(with(mapper) { plant.toEntity() })

        return try {
            firestoreDataSource.addPlant(uid, plant)
            Result.success(Unit)
        } catch (e: Exception) {
            // Decisión: NO revertir el guardado local. La planta ya está en
            // Room (fuente de verdad offline-first) y el usuario la ve en
            // "Mi Jardín" de inmediato; si la red falla, se sincronizará
            // más adelante. Revertir dejaría al usuario sin la planta que
            // acaba de guardar, que es peor para el uso en campo.
            Result.failure(Exception("Guardada en tu dispositivo. No pudimos sincronizarla con el servidor.", e))
        }
    }

    override suspend fun deletePlant(gardenPlantId: String): Result<Unit> {
        val uid = currentUidOrNull() ?: return Result.failure(IllegalStateException("Sesión no iniciada"))

        gardenDao.deleteById(gardenPlantId)

        return try {
            firestoreDataSource.deletePlant(uid, gardenPlantId)
            Result.success(Unit)
        } catch (e: Exception) {
            // Decisión: NO revertir el borrado local. Si el usuario eliminó
            // la planta y se quedó sin conexión, es más coherente para la UX
            // de campo (sin señal en la Jalca) que la planta siga desaparecida
            // localmente; Firestore quedará desincronizado hasta la próxima
            // sincronización exitosa — aceptable para el MVP, documentado aquí.
            Result.failure(Exception("Se eliminó localmente, pero no pudimos sincronizar con el servidor.", e))
        }
    }

    /**
     * UID del usuario actual para operar sobre `users/{uid}` en Firestore.
     *
     * Funciona tanto para cuentas de Google como para cuentas locales: en el
     * segundo caso el repositorio de auth expone el UID de la sesión anónima
     * de Firebase, que es lo que permite a la cuenta local escribir en
     * Firestore bajo las mismas reglas de seguridad.
     */
    /**
     * Obtiene una planta guardada desde la caché local (Room).
     *
     * Qué:    devuelve la planta del jardín con todos sus datos.
     * Cómo:   consulta `garden_plants` por `catalogPlantId` y la mapea al
     *         modelo de dominio.
     * Por qué: permite abrir la ficha de una planta guardada sin conexión,
     *         que es el requisito de "detalle offline" (RU-07).
     */
    override suspend fun getSavedPlant(catalogPlantId: String): GardenPlant? = withContext(Dispatchers.IO) {
        gardenDao.findByCatalogPlantId(catalogPlantId)?.let { with(mapper) { it.toDomain() } }
    }

    private suspend fun currentUidOrNull(): String? {
        val state = authRepository.authState.first()
        return (state as? AuthState.Authenticated)?.userId
    }
}
