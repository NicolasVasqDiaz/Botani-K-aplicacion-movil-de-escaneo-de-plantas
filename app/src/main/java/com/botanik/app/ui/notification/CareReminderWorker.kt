package com.botanik.app.ui.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CareReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val gardenPlantId = inputData.getString(KEY_GARDEN_PLANT_ID) ?: return Result.failure()
        val commonName = inputData.getString(KEY_COMMON_NAME) ?: return Result.failure()
        val scientificName = inputData.getString(KEY_SCIENTIFIC_NAME) ?: return Result.failure()

        NotificationHelper.showCareReminder(
            context = applicationContext,
            notificationId = gardenPlantId.hashCode(),
            plantCommonName = commonName,
            plantScientificName = scientificName
        )
        return Result.success()
    }

    companion object {
        const val KEY_GARDEN_PLANT_ID = "gardenPlantId"
        const val KEY_COMMON_NAME = "commonName"
        const val KEY_SCIENTIFIC_NAME = "scientificName"
    }
}