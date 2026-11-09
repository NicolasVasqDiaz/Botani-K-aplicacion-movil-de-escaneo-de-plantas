package com.botanik.app.di

import android.content.Context
import androidx.room.Room
import com.botanik.app.data.local.BotaniKDatabase
import com.botanik.app.data.local.CatalogDao
import com.botanik.app.data.local.CatalogPreloader
import com.botanik.app.data.local.GardenDao
import com.botanik.app.data.local.GardenTypeConverters
import com.botanik.app.data.local.LocalUserDao
import com.botanik.app.data.local.PlantNoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BotaniKDatabase =
        Room.databaseBuilder(context, BotaniKDatabase::class.java, "botanik_db")
            .addMigrations(
                BotaniKDatabase.MIGRATION_3_4,
                BotaniKDatabase.MIGRATION_4_5,
                BotaniKDatabase.MIGRATION_5_6
            )
            .build()

    @Provides
    fun provideGardenDao(db: BotaniKDatabase): GardenDao = db.gardenDao()

    @Provides
    fun provideLocalUserDao(db: BotaniKDatabase): LocalUserDao = db.localUserDao()

    @Provides
    fun provideCatalogDao(db: BotaniKDatabase): CatalogDao = db.catalogDao()

    @Provides
    fun providePlantNoteDao(db: BotaniKDatabase): PlantNoteDao = db.plantNoteDao()

    @Provides
    @Singleton
    fun provideDatabaseWarmup(
        dao: GardenDao,
        preloader: CatalogPreloader
    ): DatabaseWarmup = DatabaseWarmup(dao)

    @Provides
    @Singleton
    fun provideGardenTypeConverters(): GardenTypeConverters = GardenTypeConverters()
}