package com.botanik.app.data.local

import com.botanik.app.data.mapper.CatalogMapper
import com.botanik.app.data.remote.FirestoreCatalogDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Precarga el catálogo en la caché local al iniciar la app.
 *
 * Qué:    descarga el catálogo de Firestore y lo guarda en Room.
 * Cómo:   corre en segundo plano al arrancar; si falla (sin red), la app
 *         sigue funcionando con la copia que ya hubiera en la caché.
 * Por qué: para que el catálogo esté disponible sin conexión desde la
 *         primera vez que se abre la app, sin que el usuario tenga que
 *         entrar al catálogo para que se descache.
 */
@Singleton
class CatalogPreloader @Inject constructor(
    private val firestoreDataSource: FirestoreCatalogDataSource,
    private val catalogDao: CatalogDao,
    private val mapper: CatalogMapper
) {
    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                refresh()
            } catch (_: Exception) {
                // Sin red o sin permiso: la app usará la copia local cuando exista.
            }
        }
    }

    private suspend fun refresh() {
        val plants = firestoreDataSource.fetchAll()
        if (plants.isNotEmpty()) {
            val now = System.currentTimeMillis()
            catalogDao.replaceAll(plants.map { with(mapper) { it.toEntity(now) } })
        }
    }
}
