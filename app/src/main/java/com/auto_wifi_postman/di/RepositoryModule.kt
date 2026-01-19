package com.auto_wifi_postman.di

import com.auto_wifi_postman.data.repository.JsonKnownNetworksRepository
import com.auto_wifi_postman.data.repository.KnownNetworksRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideKnownNetworksRepository(
        impl: JsonKnownNetworksRepository
    ): KnownNetworksRepository = impl
}
