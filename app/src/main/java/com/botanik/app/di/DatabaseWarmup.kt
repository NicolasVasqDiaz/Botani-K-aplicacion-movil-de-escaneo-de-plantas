package com.botanik.app.di

import com.botanik.app.data.local.GardenDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fuerza la creación de la base de datos al iniciar la app.
 * Room es perezoso: solo crea el archivo cuando se ejecuta la primera consulta.
 * Al inyectar este objeto se garantiza que botanik_db exista desde el arranque.
 */
@Singleton
class DatabaseWarmup @Inject constructor(
    private val dao: GardenDao
) {
    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                dao.observeAll().first()
            } catch (_: Exception) {
                // La base de datos se creará cuando se acceda normalmente.
            }
        }
    }
}
