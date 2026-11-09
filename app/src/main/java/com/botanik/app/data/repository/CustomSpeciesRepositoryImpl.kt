package com.botanik.app.data.repository

import com.botanik.app.data.remote.FirestoreCustomSpeciesDataSource
import com.botanik.app.domain.model.CustomSpecies
import com.botanik.app.domain.repository.CustomSpeciesRepository
import javax.inject.Inject

class CustomSpeciesRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreCustomSpeciesDataSource
) : CustomSpeciesRepository {

    override suspend fun saveCustomSpecies(species: CustomSpecies): Result<Unit> =
        dataSource.add(species)

    override suspend fun getCustomSpecies(speciesId: String): Result<CustomSpecies?> =
        dataSource.getById(speciesId)

    override suspend fun getAllCustomSpecies(): Result<List<CustomSpecies>> =
        dataSource.getAll()
}