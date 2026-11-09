package com.botanik.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GardenPlantEntity::class, LocalUserEntity::class, CatalogPlantEntity::class, PlantNoteEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(GardenTypeConverters::class)
abstract class BotaniKDatabase : RoomDatabase() {
    abstract fun gardenDao(): GardenDao
    abstract fun localUserDao(): LocalUserDao
    abstract fun catalogDao(): CatalogDao
    abstract fun plantNoteDao(): PlantNoteDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE garden_plants ADD COLUMN mlConfidence REAL")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE garden_plants ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE garden_plants ADD COLUMN reminderFrequencyDays INTEGER NOT NULL DEFAULT 7")
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `plant_notes` (`noteId` TEXT NOT NULL, `gardenPlantId` TEXT NOT NULL, `text` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`noteId`))")
            }
        }
    }
}