package com.botanik.app.data.mapper

import com.botanik.app.data.local.GardenPlantEntity
import com.botanik.app.data.local.GardenTypeConverters
import com.botanik.app.domain.model.GardenPlant
import javax.inject.Inject

class GardenMapper @Inject constructor(
    private val converters: GardenTypeConverters
) {
    fun GardenPlantEntity.toDomain(): GardenPlant = GardenPlant(
        gardenPlantId = gardenPlantId,
        catalogPlantId = catalogPlantId,
        commonName = commonName,
        scientificName = scientificName,
        family = family,
        region = region,
        floweringSeason = floweringSeason,
        care = converters.toCareMap(careJson),
        funFacts = converters.toFunFactsList(funFactsJson),
        userPhotoUrl = userPhotoUrl,
        savedAt = savedAt
    )

    fun GardenPlant.toEntity(): GardenPlantEntity = GardenPlantEntity(
        gardenPlantId = gardenPlantId,
        catalogPlantId = catalogPlantId,
        commonName = commonName,
        scientificName = scientificName,
        family = family,
        region = region,
        floweringSeason = floweringSeason,
        careJson = converters.fromCareMap(care),
        funFactsJson = converters.fromFunFactsList(funFacts),
        userPhotoUrl = userPhotoUrl,
        savedAt = savedAt
    )
}
