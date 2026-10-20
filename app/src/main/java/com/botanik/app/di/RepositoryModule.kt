package com.botanik.app.di

import com.botanik.app.data.repository.AuthRepositoryImpl
import com.botanik.app.data.repository.CatalogRepositoryImpl
import com.botanik.app.data.repository.CloudinaryRepositoryImpl
import com.botanik.app.data.repository.GardenRepositoryImpl
import com.botanik.app.data.repository.UserRepositoryImpl
import com.botanik.app.domain.repository.AuthRepository
import com.botanik.app.domain.repository.CatalogRepository
import com.botanik.app.domain.repository.CloudinaryRepository
import com.botanik.app.domain.repository.GardenRepository
import com.botanik.app.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository

    @Binds
    @Singleton
    abstract fun bindGardenRepository(impl: GardenRepositoryImpl): GardenRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindCloudinaryRepository(impl: CloudinaryRepositoryImpl): CloudinaryRepository
}
