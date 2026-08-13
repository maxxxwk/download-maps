package com.download.maps.di.modules

import android.content.Context
import androidx.work.WorkManager
import com.download.maps.data.DownloadMapQueueManager
import com.download.maps.di.qualifiers.DispatcherDefault
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object DownloadMapQueueManagerModule {
    @Provides
    @Singleton
    fun provideDownloadMapQueueManager(
        workManager: WorkManager,
        @ApplicationContext context: Context,
        @DispatcherDefault dispatcher: CoroutineDispatcher
    ): DownloadMapQueueManager {
        return DownloadMapQueueManager(
            workManager = workManager,
            context = context,
            scope = CoroutineScope(SupervisorJob() + dispatcher)
        )
    }
}
