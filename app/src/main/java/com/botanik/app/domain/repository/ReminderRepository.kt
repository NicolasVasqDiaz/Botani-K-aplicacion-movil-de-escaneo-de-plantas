package com.botanik.app.domain.repository

import com.botanik.app.domain.model.GardenPlant

interface ReminderRepository {
    fun scheduleReminder(plant: GardenPlant): Result<Unit>
    fun cancelReminder(gardenPlantId: String)
}