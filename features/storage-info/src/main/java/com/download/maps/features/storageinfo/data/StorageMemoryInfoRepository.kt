package com.download.maps.features.storageinfo.data

import android.os.Environment
import android.os.StatFs
import com.download.maps.common.di.qualifiers.DispatcherIO
import com.download.maps.features.storageinfo.data.model.StorageMemoryInfo
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

internal class StorageMemoryInfoRepository @Inject constructor(
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher
) {
    fun observeStorageMemoryInfo(pollIntervalMs: Long = 2000L): Flow<StorageMemoryInfo?> = flow {
        while (currentCoroutineContext().isActive) {
            emit(
                runCatching {
                    val statFs = StatFs(Environment.getDataDirectory().absolutePath)
                    StorageMemoryInfo(
                        totalBytes = statFs.totalBytes,
                        freeBytes = statFs.availableBytes
                    )
                }.getOrNull()
            )
            delay(pollIntervalMs.milliseconds)
        }
    }.distinctUntilChanged().flowOn(dispatcher)
}
