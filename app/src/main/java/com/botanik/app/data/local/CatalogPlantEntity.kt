package com.botanik.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Copia local de una planta del catálogo.
 *
 * El catálogo es un recurso de referencia compartido (colección `catalog` en
 * Firestore). Se guarda en Room para que la app pueda consultarlo sin conexión
 * — requisito de "disponible también sin conexión mediante caché local" —
 * y para que el usuario pueda explorar especies en campo, donde la señal
 * es escasa o nula.
 */
@Entity(tableName = "catalog_plants")
data class CatalogPlantEntity(
    @PrimaryKey val id: String,
    val commonName: String,
    val scientificName: String,
    val family: String,
    val region: String,
    val floweringSeason: String,
    val careJson: String,
    val funFactsJson: String,
    val imageUrl: String,
    val isEndemic: Boolean,
    val cachedAt: Long
)
