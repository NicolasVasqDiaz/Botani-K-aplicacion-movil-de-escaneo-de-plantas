package com.botanik.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [GardenPlantEntity::class, LocalUserEntity::class, CatalogPlantEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(GardenTypeConverters::class)
abstract class BotaniKDatabase : RoomDatabase() {
    abstract fun gardenDao(): GardenDao
    abstract fun localUserDao(): LocalUserDao
    abstract fun catalogDao(): CatalogDao
}
