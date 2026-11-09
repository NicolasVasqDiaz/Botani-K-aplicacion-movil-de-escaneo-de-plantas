package com.botanik.app.data.repository

import com.botanik.app.data.local.GardenDao
import com.botanik.app.data.local.PlantNoteDao
import com.botanik.app.data.local.PlantNoteEntity
import com.botanik.app.data.mapper.GardenMapper
import com.botanik.app.data.remote.FirestoreGardenDataSource
import com.botanik.app.domain.model.AuthState
import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.domain.model.CustomSpecies
import com.botanik.app.domain.model.GardenPlant
import com.botanik.app.domain.model.PlantNote
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

class GardenRepositoryImpl @Inject constructor(
    private val gardenDao: GardenDao,
    private val plantNoteDao: PlantNoteDao,
    private val firestoreDataSource: FirestoreGardenDataSource,
    private val authRepository: AuthRepository,
    private val mapper: GardenMapper
) : GardenRepository {

    override fun getGarden(): Flow<UiState<List<GardenPlant>>> = flow {
        emit(UiState.Loading)

        val roomPlants = gardenDao.observeAll().first().map { with(mapper) { it.toDomain() } }
        emit(if (roomPlants.isEmpty()) UiState.Empty else UiState.Success(roomPlants))

        val uid = currentUidOrNull() ?: return@flow
        firestoreDataSource.observeGarden(uid)
            .catch { }
            .collect { firestorePlants ->
                val merged = mergeGardens(roomPlants, firestorePlants)
                emit(if (merged.isEmpty()) UiState.Empty else UiState.Success(merged))
            }
    }

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

    override suspend fun savePlant(
        catalogPlant: CatalogPlant,
        userPhotoUrl: String?,
        mlConfidence: Float?
    ): Result<Unit> {
        val uid = currentUidOrNull()
            ?: return Result.failure(IllegalStateException("Sesión no iniciada"))

        // VALIDACIÓN: Si ya existe, no hacemos nada.
        val existing = gardenDao.findByCatalogPlantId(catalogPlant.id)
        if (existing != null) {
            return Result.success(Unit)
        }

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
            savedAt = System.currentTimeMillis(),
            mlConfidence = mlConfidence,
            reminderEnabled = false,
            reminderFrequencyDays = 7
        )

        gardenDao.upsert(with(mapper) { plant.toEntity() })

        return try {
            firestoreDataSource.addPlant(uid, plant)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Guardada en dispositivo.", e))
        }
    }

    override suspend fun saveCustomPlant(
        species: CustomSpecies,
        userPhotoUrl: String,
        mlConfidence: Float?
    ): Result<Unit> {
        val uid = currentUidOrNull()
            ?: return Result.failure(IllegalStateException("Sesión no iniciada"))

        val customCatalogId = "custom_${species.speciesId}"
        val existing = gardenDao.findByCatalogPlantId(customCatalogId)
        if (existing != null) return Result.success(Unit)

        val plant = GardenPlant(
            gardenPlantId = UUID.randomUUID().toString(),
            catalogPlantId = customCatalogId,
            commonName = species.commonName,
            scientificName = species.scientificName,
            family = species.family,
            region = species.region,
            floweringSeason = species.floweringSeason,
            care = species.care,
            funFacts = species.funFacts,
            userPhotoUrl = userPhotoUrl,
            savedAt = System.currentTimeMillis(),
            mlConfidence = mlConfidence ?: species.confidence,
            reminderEnabled = false,
            reminderFrequencyDays = 7
        )

        gardenDao.upsert(with(mapper) { plant.toEntity() })

        return try {
            firestoreDataSource.addPlant(uid, plant)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Guardada en dispositivo.", e))
        }
    }

    override suspend fun deletePlant(gardenPlantId: String): Result<Unit> {
        val uid = currentUidOrNull() ?: return Result.failure(IllegalStateException("Sesión no iniciada"))
        gardenDao.deleteById(gardenPlantId)
        return try {
            firestoreDataSource.deletePlant(uid, gardenPlantId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Eliminada localmente.", e))
        }
    }

    override suspend fun getSavedPlant(id: String): GardenPlant? =
        withContext(Dispatchers.IO) {
            var entity = gardenDao.findByCatalogPlantId(id)
            if (entity == null) entity = gardenDao.findByGardenPlantId(id)
            entity?.let { with(mapper) { it.toDomain() } }
        }

    override suspend fun getGardenPlantById(gardenPlantId: String): GardenPlant? =
        withContext(Dispatchers.IO) {
            gardenDao.findByGardenPlantId(gardenPlantId)?.let { with(mapper) { it.toDomain() } }
        }

    override suspend fun correctGardenPlant(
        gardenPlantId: String,
        corrected: CatalogPlant
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = currentUidOrNull() ?: return@withContext Result.failure(Exception("Sesión no iniciada"))
            val existingEntity = gardenDao.findByGardenPlantId(gardenPlantId) ?: return@withContext Result.failure(Exception("Planta no encontrada"))
            val existing = with(mapper) { existingEntity.toDomain() }

            val correctedPlant = existing.copy(
                catalogPlantId = corrected.id,
                commonName = corrected.commonName,
                scientificName = corrected.scientificName,
                family = corrected.family,
                region = corrected.region,
                floweringSeason = corrected.floweringSeason,
                care = corrected.care,
                funFacts = corrected.funFacts
            )

            gardenDao.upsert(with(mapper) { correctedPlant.toEntity() })
            firestoreDataSource.updatePlant(uid, correctedPlant)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al corregir: ${e.message}", e))
        }
    }

    override suspend fun updateReminder(
        gardenPlantId: String,
        enabled: Boolean,
        frequencyDays: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val entity = gardenDao.findByGardenPlantId(gardenPlantId) ?: return@withContext Result.failure(Exception("Planta no encontrada"))
            val updated = entity.copy(reminderEnabled = enabled, reminderFrequencyDays = frequencyDays)
            gardenDao.upsert(updated)
            val uid = currentUidOrNull()
            if (uid != null) firestoreDataSource.updatePlant(uid, with(mapper) { updated.toDomain() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar: ${e.message}", e))
        }
    }

    override suspend fun getPlantsWithReminder(): List<GardenPlant> = withContext(Dispatchers.IO) {
        gardenDao.getPlantsWithReminder().map { with(mapper) { it.toDomain() } }
    }

    override suspend fun getNotesForPlant(gardenPlantId: String): List<PlantNote> = withContext(Dispatchers.IO) {
        plantNoteDao.getNotes(gardenPlantId).first().map {
            PlantNote(it.noteId, it.gardenPlantId, it.text, it.createdAt)
        }
    }

    override suspend fun addNote(note: PlantNote): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            plantNoteDao.insert(PlantNoteEntity(note.noteId, note.gardenPlantId, note.text, note.createdAt))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al guardar nota", e))
        }
    }

    private suspend fun currentUidOrNull(): String? {
        val state = authRepository.authState.first()
        return (state as? AuthState.Authenticated)?.userId
    }
}