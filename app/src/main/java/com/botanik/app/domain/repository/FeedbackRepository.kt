package com.botanik.app.domain.repository

import com.botanik.app.domain.model.PlantFeedback

interface FeedbackRepository {
    suspend fun submitFeedback(feedback: PlantFeedback): Result<Unit>
    suspend fun getFeedbackForPlant(gardenPlantId: String): Result<PlantFeedback?>
}