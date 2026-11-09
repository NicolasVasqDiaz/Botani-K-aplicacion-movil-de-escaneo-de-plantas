package com.botanik.app.data.repository

import com.botanik.app.data.remote.FirestoreFeedbackDataSource
import com.botanik.app.domain.model.PlantFeedback
import com.botanik.app.domain.repository.FeedbackRepository
import javax.inject.Inject

class FeedbackRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreFeedbackDataSource
) : FeedbackRepository {

    override suspend fun submitFeedback(feedback: PlantFeedback): Result<Unit> =
        dataSource.add(feedback)

    override suspend fun getFeedbackForPlant(gardenPlantId: String): Result<PlantFeedback?> =
        dataSource.getByGardenPlantId(gardenPlantId)
}