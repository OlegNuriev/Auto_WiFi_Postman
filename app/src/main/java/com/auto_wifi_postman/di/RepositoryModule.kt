package com.auto_wifi_postman.di

import com.auto_wifi_postman.data.repository.KnownNetworksRepository
import com.auto_wifi_postman.data.repository.KnownNetworksRepositoryImpl
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
    abstract fun bindKnownNetworksRepository(
        impl: KnownNetworksRepositoryImpl
    ): KnownNetworksRepository
}

