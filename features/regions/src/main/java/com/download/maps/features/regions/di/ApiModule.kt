package com.download.maps.features.regions.di

import com.download.maps.features.regions.data.api.DownloadService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
internal object ApiModule {
    @Provides
    @Singleton
    fun provideDownloadService(retrofit: Retrofit): DownloadService = retrofit.create()
}
