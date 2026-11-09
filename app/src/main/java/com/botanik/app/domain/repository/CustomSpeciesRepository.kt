package com.botanik.app.domain.repository

import com.botanik.app.domain.model.CustomSpecies

interface CustomSpeciesRepository {
    suspend fun saveCustomSpecies(species: CustomSpecies): Result<Unit>
    suspend fun getCustomSpecies(speciesId: String): Result<CustomSpecies?>
    suspend fun getAllCustomSpecies(): Result<List<CustomSpecies>>
}