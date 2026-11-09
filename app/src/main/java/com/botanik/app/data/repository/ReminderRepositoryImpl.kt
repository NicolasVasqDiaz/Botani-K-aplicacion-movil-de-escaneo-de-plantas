package com.botanik.app.data.repository

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.botanik.app.domain.model.GardenPlant
import com.botanik.app.domain.repository.ReminderRepository
import com.botanik.app.ui.notification.CareReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ReminderRepository {

    override fun scheduleReminder(plant: GardenPlant): Result<Unit> {
        return try {
            val data = workDataOf(
                CareReminderWorker.KEY_GARDEN_PLANT_ID to plant.gardenPlantId,
                CareReminderWorker.KEY_COMMON_NAME to plant.commonName,
                CareReminderWorker.KEY_SCIENTIFIC_NAME to plant.scientificName
            )

            // PRUEBA TEMPORAL: 10 SEGUNDOS
            val request = OneTimeWorkRequestBuilder<CareReminderWorker>()
                .setInitialDelay(10, TimeUnit.SECONDS)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "care_reminder_${plant.gardenPlantId}",
                ExistingWorkPolicy.REPLACE,
                request
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("No pudimos programar: ${e.message}", e))
        }
    }

    override fun cancelReminder(gardenPlantId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("care_reminder_$gardenPlantId")
    }
}