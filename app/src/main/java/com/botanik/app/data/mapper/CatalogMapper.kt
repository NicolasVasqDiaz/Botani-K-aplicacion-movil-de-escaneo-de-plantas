package com.botanik.app.data.mapper

import com.botanik.app.data.local.CatalogPlantEntity
import com.botanik.app.data.local.GardenTypeConverters
import com.botanik.app.domain.model.CatalogPlant
import javax.inject.Inject

/**
 * Mapper entre la entidad local del catálogo y el modelo de dominio.
 */
class CatalogMapper @Inject constructor(
    private val converters: GardenTypeConverters
) {
    fun CatalogPlantEntity.toDomain(): CatalogPlant = CatalogPlant(
        id = id,
        commonName = commonName,
        scientificName = scientificName,
        family = family,
        region = region,
        floweringSeason = floweringSeason,
        care = converters.toCareMap(careJson),
        funFacts = converters.toFunFactsList(funFactsJson),
        imageUrl = imageUrl,
        isEndemic = isEndemic
    )

    fun CatalogPlant.toEntity(cachedAt: Long): CatalogPlantEntity = CatalogPlantEntity(
        id = id,
        commonName = commonName,
        scientificName = scientificName,
        family = family,
        region = region,
        floweringSeason = floweringSeason,
        careJson = converters.fromCareMap(care),
        funFactsJson = converters.fromFunFactsList(funFacts),
        imageUrl = imageUrl,
        isEndemic = isEndemic,
        cachedAt = cachedAt
    )
}
