package com.download.maps.screens.main.data

import android.os.Environment
import android.os.StatFs
import com.download.maps.di.qualifiers.DispatcherIO
import com.download.maps.screens.main.data.model.StorageInfo
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

class StorageRepository @Inject constructor(
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher
) {
    fun observeStorageInfo(pollIntervalMs: Long = 2000L): Flow<StorageInfo?> = flow {
        while (currentCoroutineContext().isActive) {
            emit(
                runCatching {
                    val statFs = StatFs(Environment.getDataDirectory().absolutePath)
                    StorageInfo(
                        totalBytes = statFs.totalBytes,
                        freeBytes = statFs.availableBytes
                    )
                }.getOrNull()
            )
            delay(pollIntervalMs.milliseconds)
        }
    }.distinctUntilChanged().flowOn(dispatcher)
}
