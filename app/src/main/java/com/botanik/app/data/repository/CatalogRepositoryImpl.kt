package com.botanik.app.data.repository

import com.botanik.app.data.local.CatalogDao
import com.botanik.app.data.mapper.CatalogMapper
import com.botanik.app.data.remote.FirestoreCatalogDataSource
import com.botanik.app.domain.model.CatalogPlant
import com.botanik.app.domain.repository.CatalogRepository
import com.botanik.app.ui.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repositorio del catálogo de plantas con estrategia offline-first.
 *
 * Qué:    entrega la lista de especies del catálogo y su detalle.
 * Cómo:   Firestore es la fuente original; Room guarda una copia local.
 *         Al leer se sirve la copia local de inmediato (rápido y sin red)
 *         y en segundo plano se refresca desde Firestore, updating la caché.
 * Por qué: el requisito de "disponible también sin conexión" implica que
 *         el catálogo —un recurso de referencia— pueda consultarse en campo,
 *         donde la cobertura es escasa o nula.
 */
class CatalogRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreCatalogDataSource,
    private val catalogDao: CatalogDao,
    private val mapper: CatalogMapper
) : CatalogRepository {

    /** Señaliza si la última lectura provino de la caché local. */
    private val _isServingFromCache = MutableStateFlow(false)
    override val isServingFromCache: Flow<Boolean> = _isServingFromCache.asStateFlow()

    override fun getAll(): Flow<UiState<List<CatalogPlant>>> = flow {
        emit(UiState.Loading)

        // 1. Copia local primero (offline-first, inmediato).
        val cached = readLocal()
        if (cached.isNotEmpty()) {
            emit(UiState.Success(cached))
        }

        // 2. Refresco desde Firestore; si falla, se conserva la copia local.
        try {
            val remote = dataSource.fetchAll()
            if (remote.isNotEmpty()) {
                writeLocal(remote)
                _isServingFromCache.value = false
                emit(UiState.Success(remote))
            } else if (cached.isEmpty()) {
                _isServingFromCache.value = false
                emit(UiState.Empty)
            }
        } catch (e: Exception) {
            if (cached.isEmpty()) {
                _isServingFromCache.value = false
                emit(UiState.Error("No pudimos cargar el catálogo. Verifica tu conexión."))
            } else {
                _isServingFromCache.value = true
            }
            // Con copia local disponible se mantiene lo ya emitido.
        }
    }

    override fun getById(id: String): Flow<UiState<CatalogPlant>> = flow {
        emit(UiState.Loading)
        try {
            val plant = dataSource.fetchById(id)
            if (plant != null) {
                writeLocal(listOf(plant))
                emit(UiState.Success(plant))
            } else {
                emit(UiState.Error("No encontramos esa planta"))
            }
        } catch (e: Exception) {
            // Fallback offline: se busca en la copia local.
            val cached = readLocal().firstOrNull { it.id == id }
            if (cached != null) {
                emit(UiState.Success(cached))
            } else {
                emit(UiState.Error("No pudimos cargar la planta. Verifica tu conexión."))
            }
        }
    }

    override fun search(query: String): Flow<UiState<List<CatalogPlant>>> = flow {
        if (query.isBlank()) {
            emitAll(getAll())
            return@flow
        }
        emit(UiState.Loading)
        try {
            val filtered = dataSource.fetchAll().filter {
                it.commonName.contains(query, ignoreCase = true) ||
                    it.scientificName.contains(query, ignoreCase = true)
            }
            emit(if (filtered.isEmpty()) UiState.Empty else UiState.Success(filtered))
        } catch (e: Exception) {
            // Fallback offline: la búsqueda se resuelve sobre la copia local.
            val local = readLocal()
            val filtered = local.filter {
                it.commonName.contains(query, ignoreCase = true) ||
                    it.scientificName.contains(query, ignoreCase = true)
            }
            if (filtered.isNotEmpty()) {
                emit(UiState.Success(filtered))
            } else if (local.isNotEmpty()) {
                emit(UiState.Empty)
            } else {
                emit(UiState.Error("No pudimos buscar en el catálogo. Verifica tu conexión."))
            }
        }
    }

    /** Lee la copia local del catálogo. */
    private suspend fun readLocal(): List<CatalogPlant> = withContext(Dispatchers.IO) {
        catalogDao.getAll().map { with(mapper) { it.toDomain() } }
    }

    /** Actualiza la copia local del catálogo con los datos de Firestore. */
    private suspend fun writeLocal(plants: List<CatalogPlant>) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        catalogDao.replaceAll(plants.map { with(mapper) { it.toEntity(now) } })
    }
}
